package org.example.db;

import org.example.model.UserForm;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;

/**
 Тесты для класса FormRepository, который управляет анкетами пользователей в SQLite.

 Проверяются основные операции:
 - добавление и обновление анкет (upsert);
 - получение анкеты (get);
 - получение списка имён (listNames);
 - удаление анкеты (delete).

 Перед каждым тестом таблица forms очищается,
 чтобы гарантировать чистую базу данных и независимость тестов.
 */
class FormRepositoryTest {

    FormRepository repo;

    /**
     Подготавливает тестовую среду перед каждым тестом.
     Создаёт экземпляр FormRepository и очищает таблицу forms.
     */
    @BeforeEach
    void setUp() throws SQLException {
        repo = new FormRepository();

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:forms.db");
             Statement st = c.createStatement()) {
            st.execute("DELETE FROM forms");
        }
    }

    /**
     Проверяет, что метод upsert() корректно сохраняет новую анкету.
     */
    @Test
    void shouldInsertNewForm() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 45, "сад", 3000);
        repo.upsert(f);

        UserForm loaded = repo.get(1L, "Мама");
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals("сад", loaded.hobbies);
        Assertions.assertEquals(3000, loaded.budget);
    }

    /**
     Проверяет, что повторный вызов upsert() обновляет существующую анкету.
     */
    @Test
    void shouldUpdateExistingForm() {
        UserForm f1 = new UserForm(1L, "Брат", "брат", "НГ", 25, "спорт", 5000);
        repo.upsert(f1);

        UserForm f2 = new UserForm(1L, "Брат", "брат", "ДР", 26, "рыбалка", 7000);
        repo.upsert(f2);

        UserForm updated = repo.get(1L, "Брат");
        Assertions.assertEquals("ДР", updated.occasion);
        Assertions.assertEquals("рыбалка", updated.hobbies);
        Assertions.assertEquals(7000, updated.budget);
    }

    /**
     Проверяет, что метод get() возвращает null, если анкета отсутствует.
     */
    @Test
    void shouldReturnNullIfFormNotFound() {
        UserForm result = repo.get(99L, "Несуществующая");
        Assertions.assertNull(result);
    }

    /**
     Проверяет, что метод listNames() возвращает правильный список имён анкет.
     */
    @Test
    void shouldListAllNamesForUser() {
        repo.upsert(new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 2000));
        repo.upsert(new UserForm(1L, "Папа", "папа", "НГ", 50, "охота", 3000));

        List<String> names = repo.listNames(1L);
        Assertions.assertEquals(2, names.size());
        Assertions.assertTrue(names.contains("Мама"));
        Assertions.assertTrue(names.contains("Папа"));
    }

    /**
     Проверяет, что метод delete() корректно удаляет анкету из базы.
     */
    @Test
    void shouldDeleteForm() {
        repo.upsert(new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 2000));
        repo.delete(1L, "Мама");

        UserForm result = repo.get(1L, "Мама");
        Assertions.assertNull(result);
    }

    /**
     Проверяет, что удаление несуществующей анкеты не вызывает ошибок.
     */
    @Test
    void shouldNotThrowWhenDeletingNonexistentForm() {
        Assertions.assertDoesNotThrow(() -> repo.delete(1L, "Несуществующая"));
    }
}
