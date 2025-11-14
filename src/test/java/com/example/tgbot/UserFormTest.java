package org.example.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Тесты для класса UserForm, который хранит данные анкеты пользователя.
 * Проверяется корректность инициализации полей, работа форматирующих методов
 * и отображение данных в различных комбинациях (полностью заполненные данные,
 * частично заполненные данные, пустой конструктор).
 */
class UserFormTest {

    /**
     * Проверяет, что конструктор с параметрами корректно присваивает значения
     * всем полям класса UserForm. Тест гарантирует, что данные анкеты не теряются
     * при создании объекта и что все переданные значения сохраняются без изменений.
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
     * Проверяет работу пустого конструктора. Все значения должны быть равны null
     * (кроме chatId, который равен 0L). Такой тест подтверждает, что объект
     * корректно создается в минимальном состоянии и не содержит случайных данных.
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
     * Проверяет корректность генерации заголовка анкеты методом prettyCardTitle.
     * Ожидаемый формат: "Анкета: <имя>". Тест подтверждает, что метод формирует
     * человеко-понятное название карточки.
     */
    @Test
    void shouldReturnPrettyCardTitle() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 45, "сад", 3000);
        Assertions.assertEquals("Анкета: Мама", f.prettyCardTitle());
    }

    /**
     * Проверяет корректное форматирование содержимого анкеты методом prettyBody.
     * Тест использует полностью заполненные данные и ожидает точный форматированный
     * многострочный текст. Таким образом подтверждается правильность структуры вывода.
     */
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

    /**
     * Проверяет поведение метода prettyBody при отсутствии данных в отдельных полях.
     * Все пустые значения должны заменяться на символ "-". Тест гарантирует, что
     * формат вывода остается стабильным и предсказуемым даже при частично пустой анкете.
     */
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

    /**
     * Проверяет форматирование анкеты, в которой заполнены только некоторые поля.
     * Это важно, потому что пользователь может вводить данные неполностью,
     * а метод prettyBody обязан корректно отображать такую анкету.
     */
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
