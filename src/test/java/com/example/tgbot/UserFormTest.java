package org.example.model;

import org.junit.jupiter.api.*;

/**
 Тесты для класса UserForm, который хранит данные анкеты пользователя.

 Проверяются:
 - корректность инициализации полей через конструктор;
 - работа пустого конструктора;
 - методы prettyCardTitle() и prettyBody();
 - корректное отображение значений null в prettyBody().
 */
class UserFormTest {

    /**
     Проверяет, что конструктор корректно инициализирует все поля объекта.
     */
    @Test
    void shouldInitializeFieldsCorrectly() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 45, "сад", 3000);

        Assertions.assertEquals(1L, f.chatId);
        Assertions.assertEquals("Мама", f.name);
        Assertions.assertEquals("мама", f.relation);
        Assertions.assertEquals("ДР", f.occasion);
        Assertions.assertEquals(45, f.age);
        Assertions.assertEquals("сад", f.hobbies);
        Assertions.assertEquals(3000, f.budget);
    }

    /**
     Проверяет, что пустой конструктор создаёт объект с полями, равными null или 0.
     */
    @Test
    void shouldCreateEmptyFormWithDefaultConstructor() {
        UserForm f = new UserForm();

        Assertions.assertEquals(0L, f.chatId);
        Assertions.assertNull(f.name);
        Assertions.assertNull(f.relation);
        Assertions.assertNull(f.occasion);
        Assertions.assertNull(f.age);
        Assertions.assertNull(f.hobbies);
        Assertions.assertNull(f.budget);
    }

    /**
     Проверяет, что prettyCardTitle() возвращает строку с корректным заголовком анкеты.
     */
    @Test
    void shouldReturnPrettyCardTitle() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 45, "сад", 3000);
        Assertions.assertEquals("Анкета: Мама", f.prettyCardTitle());
    }

    /**
     Проверяет, что prettyBody() формирует корректный форматированный текст анкеты.
     */
    @Test
    void shouldReturnFormattedBodyText() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 45, "сад", 3000);

        String text = f.prettyBody();
        Assertions.assertTrue(text.contains("Повод: ДР"));
        Assertions.assertTrue(text.contains("Возраст: 45"));
        Assertions.assertTrue(text.contains("Интересы: сад"));
        Assertions.assertTrue(text.contains("Бюджет: 3000 ₽"));
    }

    /**
     Проверяет, что prettyBody() корректно обрабатывает поля со значением null.
     */
    @Test
    void shouldHandleNullFieldsInPrettyBody() {
        UserForm f = new UserForm(1L, "Брат", null, null, null, null, null);

        String text = f.prettyBody();
        Assertions.assertTrue(text.contains("Повод: -"));
        Assertions.assertTrue(text.contains("Возраст: -"));
        Assertions.assertTrue(text.contains("Интересы: -"));
        Assertions.assertTrue(text.contains("Бюджет: -"));
    }

    /**
     Проверяет, что метод prettyBody() корректно форматирует частично заполненные данные.
     */
    @Test
    void shouldFormatPartialDataCorrectly() {
        UserForm f = new UserForm(2L, "Папа", "папа", "НГ", null, "охота", null);

        String text = f.prettyBody();
        Assertions.assertTrue(text.contains("Повод: НГ"));
        Assertions.assertTrue(text.contains("Возраст: -"));
        Assertions.assertTrue(text.contains("Интересы: охота"));
        Assertions.assertTrue(text.contains("Бюджет: -"));
    }
}
