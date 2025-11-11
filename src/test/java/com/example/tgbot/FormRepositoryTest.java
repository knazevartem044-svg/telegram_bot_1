package org.example.db;

import org.example.model.UserForm;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;

/**
 * Тесты для класса FormRepository.
 * Использует отдельную тестовую базу forms_test.db, чтобы не трогать реальные данные пользователей.
 */
class FormRepositoryTest {

    /** Репозиторий для работы с тестовой базой данных. */
    FormRepository repo;

    /**
     * Подготавливает тестовую базу данных перед каждым тестом.
     * Создает таблицу forms, если она отсутствует, и очищает все данные.
     */
    @BeforeEach
    void setUp() throws SQLException {
        repo = new FormRepository("forms_test.db");

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:forms_test.db");
             Statement st = c.createStatement()) {

            // Создаем таблицу, если её нет
            st.execute("""
                CREATE TABLE IF NOT EXISTS forms(
                    chat_id   INTEGER NOT NULL,
                    name      TEXT    NOT NULL,
                    relation  TEXT,
                    occasion  TEXT,
                    age       INTEGER,
                    hobbies   TEXT,
                    budget    INTEGER,
                    PRIMARY KEY(chat_id, name)
                )
            """);

            // Очищаем тестовую таблицу
            st.execute("DELETE FROM forms");
        }
    }

    /**
     * Проверяет, что новая анкета корректно сохраняется в базе данных.
     * После вставки данные доступны через метод get().
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
     * Проверяет, что при сохранении анкеты с теми же ключами
     * данные обновляются (upsert работает корректно).
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
     * Проверяет, что метод get() возвращает null,
     * если анкета с указанным именем не найдена.
     */
    @Test
    void shouldReturnNullIfFormNotFound() {
        UserForm result = repo.get(99L, "Несуществующая");
        Assertions.assertNull(result);
    }

    /**
     * Проверяет, что метод listNames() возвращает все имена анкет
     * для заданного пользователя.
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
     * Проверяет, что метод delete() корректно удаляет анкету из базы.
     * После удаления метод get() должен возвращать null.
     */
    @Test
    void shouldDeleteForm() {
        repo.upsert(new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 2000));
        repo.delete(1L, "Мама");

        UserForm result = repo.get(1L, "Мама");
        Assertions.assertNull(result);
    }

    /**
     * Проверяет, что метод delete() не выбрасывает исключений,
     * если удаляется несуществующая анкета.
     */
    @Test
    void shouldNotThrowWhenDeletingNonexistentForm() {
        Assertions.assertDoesNotThrow(() -> repo.delete(1L, "Несуществующая"));
    }
}
