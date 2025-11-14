package org.example.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Тесты для класса UserForm, который хранит данные анкеты пользователя.
 */
class UserFormTest {

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

    @Test
    void shouldReturnPrettyCardTitle() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 45, "сад", 3000);
        Assertions.assertEquals("Анкета: Мама", f.prettyCardTitle());
    }

    @Test
    void shouldReturnFormattedBodyText() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 45, "сад", 3000);

        String expected = """
                Повод: ДР
                Возраст: 45
                Интересы: сад
                Бюджет: 3000 ₽
                """.strip();

        Assertions.assertEquals(expected, f.prettyBody().strip());
    }

    @Test
    void shouldHandleNullFieldsInPrettyBody() {
        UserForm f = new UserForm(1L, "Брат", null, null, null, null, null);

        String expected = """
                Повод: -
                Возраст: -
                Интересы: -
                Бюджет: -
                """.strip();

        Assertions.assertEquals(expected, f.prettyBody().strip());
    }

    @Test
    void shouldFormatPartialDataCorrectly() {
        UserForm f = new UserForm(2L, "Папа", "папа", "НГ", null, "охота", null);

        String expected = """
                Повод: НГ
                Возраст: -
                Интересы: охота
                Бюджет: -
                """.strip();

        Assertions.assertEquals(expected, f.prettyBody().strip());
    }
}
