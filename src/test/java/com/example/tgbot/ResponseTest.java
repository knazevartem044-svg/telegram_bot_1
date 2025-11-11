package org.example;

import com.pengrad.telegrambot.model.request.Keyboard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Подробные тесты для класса Response.
 * Проверяется работа конструкторов, корректность хранения значений
 * и поведение при граничных ситуациях (пустой текст, null).
 */
class ResponseTest {

    /**
     * Проверяет создание объекта без клавиатуры.
     * Убеждаемся, что chatId и text сохраняются правильно, а markup равен null.
     */
    @Test
    void shouldCreateResponseWithoutKeyboard() {
        Response response = new Response(12345L, "Привет!");

        Assertions.assertEquals(12345L, response.getChatId());
        Assertions.assertEquals("Привет!", response.getText());
        Assertions.assertNull(response.getMarkup());
    }

    /**
     * Проверяет создание объекта с клавиатурой.
     * Убеждаемся, что все поля корректно сохраняются.
     */
    @Test
    void shouldCreateResponseWithKeyboard() {
        Keyboard keyboard = new Keyboard() {}; // простая заглушка клавиатуры
        Response response = new Response(54321L, "Сообщение с кнопками", keyboard);

        Assertions.assertEquals(54321L, response.getChatId());
        Assertions.assertEquals("Сообщение с кнопками", response.getText());
        Assertions.assertEquals(keyboard, response.getMarkup());
    }

    /**
     * Проверяет, что объект Response корректно хранит текстовое сообщение.
     */
    @Test
    void shouldReturnCorrectText() {
        Response response = new Response(1L, "Проверка текста");
        Assertions.assertEquals("Проверка текста", response.getText());
    }

    /**
     * Проверяет, что объект Response корректно возвращает chatId.
     */
    @Test
    void shouldReturnCorrectChatId() {
        Response response = new Response(99L, "Сообщение");
        Assertions.assertEquals(99L, response.getChatId());
    }

    /**
     * Проверяет, что метод getMarkup() возвращает null,
     * если клавиатура не была передана в конструктор.
     */
    @Test
    void shouldReturnNullMarkupWhenNotProvided() {
        Response response = new Response(42L, "Без клавиатуры");
        Assertions.assertNull(response.getMarkup());
    }

    /**
     * Проверяет создание объекта, если передан пустой текст.
     * Тест гарантирует, что пустая строка не вызывает ошибок.
     */
    @Test
    void shouldAllowEmptyText() {
        Response response = new Response(777L, "");
        Assertions.assertEquals("", response.getText());
        Assertions.assertEquals(777L, response.getChatId());
        Assertions.assertNull(response.getMarkup());
    }

    /**
     * Проверяет создание объекта, если текст равен null.
     * Конструктор не должен выбрасывать исключение.
     */
    @Test
    void shouldAllowNullText() {
        Response response = new Response(888L, null);
        Assertions.assertNull(response.getText());
        Assertions.assertEquals(888L, response.getChatId());
        Assertions.assertNull(response.getMarkup());
    }
}
