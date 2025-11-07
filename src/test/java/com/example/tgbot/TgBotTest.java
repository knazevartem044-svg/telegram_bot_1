package org.example;

import com.google.gson.Gson;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.example.db.FormRepository;
import org.example.model.UserForm;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Полный интеграционный тест бота без Mockito.
 * Работает с Java 21, использует JSON-создание Update для Telegram SDK.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TgBotTest {

    private BotLogic logic;
    private FormRepository repo;
    private final long chatId = 777000111L;
    private static final Gson gson = new Gson();

    // ---------- JSON-эмуляция Update ----------

    private static Update textUpdate(long chatId, String text) {
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

    private static Update callbackUpdate(long chatId, String data) {
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

    static class StubIdeaServiceOk extends GiftIdeaService {
        @Override
        public String fetchGiftIdeas(String prompt) {
            return "🎁 Идея 1\n🎁 Идея 2\n🎁 Идея 3";
        }
    }

    static class StubIdeaServiceFail extends GiftIdeaService {
        @Override
        public String fetchGiftIdeas(String prompt) throws IOException {
            throw new IOException("fail");
        }
    }

    private static void injectIdeaService(BotLogic logic, GiftIdeaService stub) throws Exception {
        Field f = BotLogic.class.getDeclaredField("ideaService");
        f.setAccessible(true);
        f.set(logic, stub);
    }

    // ---------- Подготовка ----------

    @BeforeEach
    void setup() {
        repo = new FormRepository();
        logic = new BotLogic();
    }

    // ---------- Тесты логики ----------

    @Test @Order(1)
    void help_command_shows_menu() {
        Response r = logic.processUpdate(textUpdate(chatId, "/help"));
        assertNotNull(r);
        assertTrue(r.getText().contains("Команды"));
        assertTrue(r.getMarkup() instanceof ReplyKeyboardMarkup);
    }

    @Test
    @Order(2)
    void forms_empty_list() {
        // Очистим все анкеты перед проверкой
        repo.listNames(chatId).forEach(n -> repo.delete(chatId, n));

        Response r = logic.processUpdate(textUpdate(chatId, "/forms"));
        String text = r.getText();

        assertTrue(
                text.contains("нет анкет") ||
                        text.contains("У вас пока нет анкет") ||
                        text.toLowerCase().contains("анкет нет"),
                "Ожидали сообщение о пустом списке анкет, но получили: " + text
        );
    }


    @Test @Order(3)
    void createform_wizard_and_save() {
        logic.processUpdate(textUpdate(chatId, "/createform"));
        logic.processUpdate(textUpdate(chatId, "Мама"));
        logic.processUpdate(textUpdate(chatId, "Маме"));
        logic.processUpdate(textUpdate(chatId, "День рождения"));
        logic.processUpdate(textUpdate(chatId, "50"));
        logic.processUpdate(textUpdate(chatId, "Сад, книги"));
        Response last = logic.processUpdate(textUpdate(chatId, "4000"));
        assertTrue(last.getText().contains("Анкета Мама сохранена"));
        assertNotNull(repo.get(chatId, "Мама"));
    }

    @Test @Order(4)
    void forms_list_and_open() {
        repo.upsert(new UserForm(chatId, "Друг", "Друг", "Новый год", 25, "Спорт", 2000));
        Response list = logic.processUpdate(textUpdate(chatId, "/forms"));
        assertTrue(list.getText().contains("Выберите анкету"));
        Response open = logic.processUpdate(callbackUpdate(chatId, "form:Друг"));
        assertTrue(open.getText().contains("Анкета: Друг"));
        assertTrue(open.getMarkup() instanceof InlineKeyboardMarkup);
    }

    @Test @Order(5)
    void edit_field_flow() {
        repo.upsert(new UserForm(chatId, "Брат", "Брат", "Праздник", 30, "Музыка", 4000));
        logic.processUpdate(callbackUpdate(chatId, "editfield:Брат:hobbies"));
        logic.processUpdate(textUpdate(chatId, "Фильмы"));
        assertEquals("Фильмы", repo.get(chatId, "Брат").hobbies);
    }

    @Test @Order(6)
    void delete_flow() {
        repo.upsert(new UserForm(chatId, "Коллега", "Коллега", "Корпоратив", 35, "Чтение", 5000));
        logic.processUpdate(callbackUpdate(chatId, "delete:Коллега"));
        logic.processUpdate(callbackUpdate(chatId, "deleteok:Коллега"));
        assertNull(repo.get(chatId, "Коллега"));
    }

    @Test @Order(7)
    void idea_generation_success() throws Exception {
        repo.upsert(new UserForm(chatId, "Мама", "Мама", "ДР", 50, "Сад", 3000));
        injectIdeaService(logic, new StubIdeaServiceOk());
        Response r = logic.processUpdate(callbackUpdate(chatId, "idea:Мама"));
        assertNotNull(r);
        assertTrue(r.getText().contains("🎁 Идея"));
    }

    @Test @Order(8)
    void idea_generation_fail() throws Exception {
        repo.upsert(new UserForm(chatId, "Папа", "Папа", "ДР", 60, "Авто", 6000));
        injectIdeaService(logic, new StubIdeaServiceFail());
        Response r = logic.processUpdate(callbackUpdate(chatId, "idea:Папа"));
        assertNotNull(r);
        assertTrue(r.getText().contains("Не удалось"));
    }

    @Test @Order(9)
    void back_to_forms_callback() {
        repo.upsert(new UserForm(chatId, "Сестра", "Сестра", "8 марта", 27, "Книги", 3000));
        Response r = logic.processUpdate(callbackUpdate(chatId, "forms:list"));
        assertNotNull(r);
        assertTrue(r.getText().contains("Выберите анкету"));
    }

    @Test @Order(10)
    void unknown_command() {
        Response r = logic.processUpdate(textUpdate(chatId, "что-то странное"));
        assertNotNull(r);
        assertTrue(r.getText().toLowerCase().contains("не понимаю"));
    }

    // ---------- Репозиторий ----------

    @Test @Order(11)
    void repository_crud() {
        FormRepository r = new FormRepository();
        UserForm f = new UserForm(chatId, "Тест", "Тест", "Повод", 20, "Интересы", 1000);
        r.upsert(f);
        assertNotNull(r.get(chatId, "Тест"));
        f.hobbies = "Новое";
        r.upsert(f);
        assertEquals("Новое", r.get(chatId, "Тест").hobbies);
        r.delete(chatId, "Тест");
        assertNull(r.get(chatId, "Тест"));
    }

    @Test @Order(12)
    void repository_list_names() throws SQLException {
        List<String> names = repo.listNames(chatId);
        assertNotNull(names);
    }

    // ---------- Клавиатуры ----------

    @Test @Order(13)
    void keyboards_check_all() {
        Keyboards kb = new Keyboards();
        assertNotNull(kb.mainReply());
        assertNotNull(kb.formList(List.of("Мама", "Друг")));
        assertNotNull(kb.formActions("Мама"));
        assertNotNull(kb.editFieldMenu("Мама"));
        assertNotNull(kb.confirmDelete("Мама"));
        assertNotNull(kb.backToForms());
    }

    // ---------- UserForm ----------

    @Test @Order(14)
    void userform_formatting() {
        UserForm f = new UserForm(chatId, "Мама", "Мама", "ДР", 50, "Сад", 3000);
        assertTrue(f.prettyBody().contains("Бюджет"));
    }

    @Test @Order(15)
    void userform_nulls() {
        UserForm f = new UserForm(chatId, "Друг", "Друг", null, null, null, null);
        assertTrue(f.prettyBody().contains("Интересы"));
    }

    // ---------- TgBot ----------

    @Test @Order(16)
    void tgbot_basic_construct() {
        TgBot bot = new TgBot("dummy");
        assertNotNull(bot);
    }
}
