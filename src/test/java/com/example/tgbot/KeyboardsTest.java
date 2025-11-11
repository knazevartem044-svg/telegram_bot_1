package org.example;

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

/**
 * Класс содержит подробные юнит-тесты для проверки всех методов класса Keyboards.
 * Проверяется создание различных типов клавиатур Telegram-бота и корректность их структуры.
 */
class KeyboardsTest {

    /** Экземпляр класса Keyboards, создаваемый перед каждым тестом. */
    Keyboards kb;

    /**
     * Выполняется перед каждым тестом.
     * Создаёт новый экземпляр Keyboards, чтобы каждый тест работал с чистым объектом.
     * Это исключает влияние предыдущих тестов на текущие результаты.
     */
    @BeforeEach
    void setUp() {
        kb = new Keyboards();
    }

    /**
     * Проверяет, что метод mainReply создаёт основную клавиатуру бота.
     * Основная клавиатура должна содержать кнопки “Помощь”, “Мои анкеты” и “Создать анкету”.
     * Проверяется, что:
     * - объект клавиатуры успешно создан и не равен null;
     * - возвращаемый тип соответствует ReplyKeyboardMarkup;
     * - сериализация объекта в строку не даёт пустое значение.
     * Таким образом подтверждается, что клавиатура корректно инициализируется и готова к использованию ботом.
     */
    @Test
    void shouldCreateMainReplyKeyboard() {
        ReplyKeyboardMarkup reply = kb.mainReply();

        Assertions.assertNotNull(reply);
        Assertions.assertEquals(ReplyKeyboardMarkup.class, reply.getClass());

        String text = reply.toString();
        Assertions.assertFalse(text.isBlank(), "Сериализованный текст клавиатуры не должен быть пустым");
    }

    /**
     * Проверяет, что метод formList создаёт клавиатуру со списком анкет пользователя.
     * В каждой строке клавиатуры должна быть кнопка, соответствующая названию анкеты.
     * Каждая кнопка содержит callback-данные вида “form:ИмяАнкеты”.
     * Проверяется, что JSON-представление содержит нужные имена и callback.
     */
    @Test
    void shouldCreateFormListKeyboard() {
        InlineKeyboardMarkup markup = kb.formList(List.of("Мама", "Брат"));
        String json = markup.toString();

        Assertions.assertTrue(json.contains("Мама"));
        Assertions.assertTrue(json.contains("Брат"));
        Assertions.assertTrue(json.contains("form:Мама"));
        Assertions.assertTrue(json.contains("form:Брат"));
    }

    /**
     * Проверяет, что метод formActions создаёт клавиатуру с действиями над анкетой.
     * Клавиатура должна включать три кнопки:
     * 1. “Отредактировать” — callback edit:ИмяАнкеты
     * 2. “Удалить” — callback delete:ИмяАнкеты
     * 3. “Сгенерировать идею” — callback idea:ИмяАнкеты
     * Проверяется, что все эти кнопки присутствуют в итоговом объекте.
     */
    @Test
    void shouldCreateFormActionsKeyboard() {
        InlineKeyboardMarkup markup = kb.formActions("Мама");
        String json = markup.toString();

        Assertions.assertTrue(json.contains("Отредактировать"));
        Assertions.assertTrue(json.contains("Удалить"));
        Assertions.assertTrue(json.contains("Сгенерировать идею"));
        Assertions.assertTrue(json.contains("edit:Мама"));
        Assertions.assertTrue(json.contains("delete:Мама"));
        Assertions.assertTrue(json.contains("idea:Мама"));
    }

    /**
     * Проверяет, что метод editFieldMenu создаёт меню редактирования полей анкеты.
     * Клавиатура должна содержать кнопки:
     * “Повод”, “Возраст”, “Интересы”, “Бюджет” и кнопку “Назад”.
     * Для каждой кнопки формируются callback-данные вида editfield:ИмяАнкеты:Поле.
     * Также присутствует кнопка возврата “Назад”, ведущая обратно к анкете.
     */
    @Test
    void shouldCreateEditFieldMenu() {
        InlineKeyboardMarkup markup = kb.editFieldMenu("Мама");
        String json = markup.toString();

        Assertions.assertTrue(json.contains("Повод"));
        Assertions.assertTrue(json.contains("Возраст"));
        Assertions.assertTrue(json.contains("Интересы"));
        Assertions.assertTrue(json.contains("Бюджет"));
        Assertions.assertTrue(json.contains("Назад"));

        Assertions.assertTrue(json.contains("editfield:Мама:occasion"));
        Assertions.assertTrue(json.contains("editfield:Мама:age"));
        Assertions.assertTrue(json.contains("editfield:Мама:hobbies"));
        Assertions.assertTrue(json.contains("editfield:Мама:budget"));
        Assertions.assertTrue(json.contains("form:Мама"));
    }

    /**
     * Проверяет, что метод confirmDelete создаёт клавиатуру для подтверждения удаления анкеты.
     * Должно быть две кнопки:
     * “Да, удалить” — выполняет удаление анкеты;
     * “Отмена” — возвращает пользователя обратно к просмотру анкеты.
     * Проверяется наличие обеих кнопок и корректных callback-данных.
     */
    @Test
    void shouldCreateDeleteConfirmation() {
        InlineKeyboardMarkup markup = kb.confirmDelete("Мама");
        String json = markup.toString();

        Assertions.assertTrue(json.contains("Да, удалить"));
        Assertions.assertTrue(json.contains("Отмена"));
        Assertions.assertTrue(json.contains("deleteok:Мама"));
        Assertions.assertTrue(json.contains("form:Мама"));
    }

    /**
     * Проверяет, что метод backToForms создаёт клавиатуру возврата к списку анкет.
     * Клавиатура должна содержать одну кнопку “К анкетам” с callback-данными “forms:list”.
     * Этот элемент используется для навигации после завершения генерации идеи подарка.
     */
    @Test
    void shouldCreateBackToFormsButton() {
        InlineKeyboardMarkup markup = kb.backToForms();
        String json = markup.toString();

        Assertions.assertTrue(json.contains("К анкетам"));
        Assertions.assertTrue(json.contains("forms:list"));
    }
}
