package org.example;

import com.google.gson.Gson;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.example.db.Database;
import org.example.db.FormRepository;
import org.example.model.UserForm;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Интеграционные и модульные тесты без использования статических полей и методов.
 * Покрывают основную логику BotLogic, работу с БД, клавиатуры и модель анкеты.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TgBotTest {

    private BotLogic logic;
    private FormRepository repo;
    private long chatId;
    private Gson gson;

    // ---------- Подготовка ----------

    @BeforeEach
    void setup() {
        chatId = 777000111L;
        gson = new Gson();
        repo = new FormRepository();
        logic = new BotLogic();
    }

    // ---------- Вспомогательные методы для JSON → Update ----------

    private Update textUpdate(String text) {
        String json = String.format("""
            {
              "update_id": 1,
              "message": {
                "message_id": 10,
                "chat": {"id": %d, "type": "private"},
                "text": "%s"
              }
            }
            """, chatId, text.replace("\"", "\\\""));
        return gson.fromJson(json, Update.class);
    }

    private Update callbackUpdate(String data) {
        String json = String.format("""
            {
              "update_id": 2,
              "callback_query": {
                "id": "123",
                "data": "%s",
                "message": {
                  "message_id": 20,
                  "chat": {"id": %d, "type": "private"}
                }
              }
            }
            """, data.replace("\"", "\\\""), chatId);
        return gson.fromJson(json, Update.class);
    }

    // ---------- Заглушки сервиса генерации идей ----------

    class StubIdeaServiceOk extends GiftIdeaService {
        @Override
        public String fetchGiftIdeas(String prompt) {
            return "Идея 1\nИдея 2\nИдея 3";
        }
    }

    class StubIdeaServiceFail extends GiftIdeaService {
        @Override
        public String fetchGiftIdeas(String prompt) throws IOException {
            throw new IOException("fail");
        }
    }

    private void injectIdeaService(GiftIdeaService stub) throws Exception {
        Field f = BotLogic.class.getDeclaredField("ideaService");
        f.setAccessible(true);
        f.set(logic, stub);
    }

    // ---------- Тесты логики BotLogic ----------

    @Test
    @Order(1)
    void help_command_shows_menu() {
        Response r = logic.processUpdate(textUpdate("/help"));
        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.getText().contains("Команды"));
        Assertions.assertTrue(r.getMarkup() instanceof ReplyKeyboardMarkup);
    }

    @Test
    @Order(2)
    void forms_empty_list() {
        for (String name: repo.listNames(chatId)) {
            repo.delete(chatId, name);
        }

        Response r = logic.processUpdate(textUpdate("/forms"));
        String text = r.getText();

        Assertions.assertTrue(
                text.contains("нет анкет")
                        || text.contains("У вас пока нет анкет")
                        || text.toLowerCase().contains("анкет нет"),
                "Ожидали сообщение о пустом списке анкет, но получили: " + text
        );
    }

    @Test
    @Order(3)
    void createform_wizard_and_save() {
        logic.processUpdate(textUpdate("/createform"));       // старт опроса
        logic.processUpdate(textUpdate("Мама"));              // название анкеты
        logic.processUpdate(textUpdate("Маме"));              // кому
        logic.processUpdate(textUpdate("День рождения"));     // повод
        logic.processUpdate(textUpdate("50"));                // возраст
        logic.processUpdate(textUpdate("Сад, книги"));        // интересы
        Response last = logic.processUpdate(textUpdate("4000")); // бюджет

        Assertions.assertTrue(last.getText().contains("Анкета Мама сохранена"));
        Assertions.assertNotNull(repo.get(chatId, "Мама"));
    }

    @Test
    @Order(4)
    void forms_list_and_open() {
        repo.upsert(new UserForm(chatId, "Друг", "Друг", "Новый год", 25, "Спорт", 2000));

        Response list = logic.processUpdate(textUpdate("/forms"));
        Assertions.assertTrue(list.getText().contains("Выберите анкету"));

        Response open = logic.processUpdate(callbackUpdate("form:Друг"));
        Assertions.assertTrue(open.getText().contains("Анкета: Друг"));
        Assertions.assertTrue(open.getMarkup() instanceof InlineKeyboardMarkup);
    }

    @Test
    @Order(5)
    void edit_field_flow() {
        repo.upsert(new UserForm(chatId, "Брат", "Брат", "Праздник", 30, "Музыка", 4000));

        logic.processUpdate(callbackUpdate("editfield:Брат:hobbies"));
        logic.processUpdate(textUpdate("Фильмы"));

        Assertions.assertEquals("Фильмы", repo.get(chatId, "Брат").hobbies);
    }

    @Test
    @Order(6)
    void delete_flow() {
        repo.upsert(new UserForm(chatId, "Коллега", "Коллега", "Корпоратив", 35, "Чтение", 5000));

        logic.processUpdate(callbackUpdate("delete:Коллега"));
        logic.processUpdate(callbackUpdate("deleteok:Коллега"));

        Assertions.assertNull(repo.get(chatId, "Коллега"));
    }

    @Test
    @Order(7)
    void idea_generation_success() throws Exception {
        repo.upsert(new UserForm(chatId, "Мама", "Мама", "ДР", 50, "Сад", 3000));

        injectIdeaService(new StubIdeaServiceOk());
        Response r = logic.processUpdate(callbackUpdate("idea:Мама"));

        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.getText().contains("Идея"));
    }

    @Test
    @Order(8)
    void idea_generation_fail() throws Exception {
        repo.upsert(new UserForm(chatId, "Папа", "Папа", "ДР", 60, "Авто", 6000));

        injectIdeaService(new StubIdeaServiceFail());
        Response r = logic.processUpdate(callbackUpdate("idea:Папа"));

        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.getText().contains("Не удалось"));
    }

    @Test
    @Order(9)
    void back_to_forms_callback() {
        repo.upsert(new UserForm(chatId, "Сестра", "Сестра", "8 марта", 27, "Книги", 3000));

        Response r = logic.processUpdate(callbackUpdate("forms:list"));

        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.getText().contains("Выберите анкету"));
    }

    @Test
    @Order(10)
    void unknown_command() {
        Response r = logic.processUpdate(textUpdate("что-то странное"));

        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.getText().toLowerCase().contains("не понимаю"));
    }

    // ---------- Репозиторий и база данных ----------

    @Test
    @Order(11)
    void repository_crud() {
        FormRepository localRepo = new FormRepository();
        UserForm f = new UserForm(chatId, "Тест", "Тест", "Повод", 20, "Интересы", 1000);

        localRepo.upsert(f);
        Assertions.assertNotNull(localRepo.get(chatId, "Тест"));

        f.hobbies = "Новое";
        localRepo.upsert(f);
        Assertions.assertEquals("Новое", localRepo.get(chatId, "Тест").hobbies);

        localRepo.delete(chatId, "Тест");
        Assertions.assertNull(localRepo.get(chatId, "Тест"));
    }

    @Test
    @Order(12)
    void repository_list_names_not_null() {
        List<String> names = repo.listNames(chatId);
        Assertions.assertNotNull(names);
    }

    @Test
    @Order(13)
    void database_connection_works() throws SQLException {
        Database db = new Database();
        try (Connection c = db.getConnection()) {
            Assertions.assertNotNull(c);
            Assertions.assertFalse(c.isClosed());
        }
    }

    // ---------- Клавиатуры ----------

    @Test
    @Order(14)
    void keyboards_check_all() {
        Keyboards kb = new Keyboards();

        Assertions.assertNotNull(kb.mainReply());
        Assertions.assertNotNull(kb.formList(List.of("Мама", "Друг")));
        Assertions.assertNotNull(kb.formActions("Мама"));
        Assertions.assertNotNull(kb.editFieldMenu("Мама"));
        Assertions.assertNotNull(kb.confirmDelete("Мама"));
        Assertions.assertNotNull(kb.backToForms());
    }

    // ---------- Модель анкеты и Response ----------

    @Test
    @Order(15)
    void userform_formatting() {
        UserForm f = new UserForm(chatId, "Мама", "Мама", "ДР", 50, "Сад", 3000);
        String body = f.prettyBody();

        Assertions.assertTrue(body.contains("Повод"));
        Assertions.assertTrue(body.contains("Возраст"));
        Assertions.assertTrue(body.contains("Интересы"));
        Assertions.assertTrue(body.contains("Бюджет"));
    }

    @Test
    @Order(16)
    void userform_nulls_are_handled() {
        UserForm f = new UserForm(chatId, "Друг", "Друг", null, null, null, null);
        String body = f.prettyBody();

        Assertions.assertTrue(body.contains("Интересы"));
    }

    @Test
    @Order(17)
    void response_getters() {
        ReplyKeyboardMarkup markup = new Keyboards().mainReply();
        Response r = new Response(chatId, "test", markup);

        Assertions.assertEquals(chatId, r.getChatId());
        Assertions.assertEquals("test", r.getText());
        Assertions.assertEquals(markup, r.getMarkup());
    }

    // ---------- TgBot и GiftIdeaService (smoke-тесты) ----------

    @Test
    @Order(18)
    void tgbot_basic_construct() {
        TgBot bot = new TgBot("dummy");
        Assertions.assertNotNull(bot);
    }

    @Test
    @Order(19)
    void giftIdeaService_constructs() {
        GiftIdeaService service = new GiftIdeaService();
        Assertions.assertNotNull(service);
    }
}
