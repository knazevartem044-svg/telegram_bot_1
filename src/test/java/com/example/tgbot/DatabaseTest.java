package org.example.db;

import org.junit.jupiter.api.*;
import java.sql.*;

/**
 Тесты для проверки работы класса Database.
 Проверяются подключение, создание таблицы и её структура.
 */
class DatabaseTest {

    /**
     Объект базы данных, используемый в тестах.
     Для безопасности создаётся отдельная тестовая база test.db.
     */
    Database db;

    /** Перед каждым тестом создаётся новая база данных */
    @BeforeEach
    void setUp() {
        db = new Database("jdbc:sqlite:test.db");
    }

    /** Проверяет, что подключение к базе устанавливается успешно */
    @Test
    void shouldReturnValidConnection() throws SQLException {
        try (Connection conn = db.getConnection()) {
            Assertions.assertNotNull(conn);
            Assertions.assertFalse(conn.isClosed());
        }
    }

    /** Проверяет, что таблица forms существует после инициализации */
    @Test
    void shouldCreateFormsTableIfNotExists() throws SQLException {
        try (Connection conn = db.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "forms", null)) {
                Assertions.assertTrue(rs.next(), "Таблица forms должна существовать");
            }
        }
    }

    /** Проверяет, что таблица forms содержит все ожидаемые столбцы */
    @Test
    void shouldHaveExpectedColumnsInFormsTable() throws SQLException {
        try (Connection conn = db.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, "forms", null)) {

            StringBuilder columns = new StringBuilder();
            while (rs.next()) {
                columns.append(rs.getString("COLUMN_NAME")).append(",");
            }

            String names = columns.toString();
            Assertions.assertTrue(names.contains("chat_id"));
            Assertions.assertTrue(names.contains("name"));
            Assertions.assertTrue(names.contains("relation"));
            Assertions.assertTrue(names.contains("occasion"));
            Assertions.assertTrue(names.contains("age"));
            Assertions.assertTrue(names.contains("hobbies"));
            Assertions.assertTrue(names.contains("budget"));
        }
    }

    /** Проверяет, что повторная инициализация не вызывает ошибок */
    @Test
    void shouldNotThrowOnRepeatedInitialization() {
        Assertions.assertDoesNotThrow(() -> new Database("jdbc:sqlite:test.db"));
    }
}
