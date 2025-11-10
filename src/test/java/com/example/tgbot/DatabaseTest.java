package org.example.db;

import org.junit.jupiter.api.*;
import java.sql.*;

/**
 Тесты для класса Database, который управляет подключением к SQLite
 и создаёт таблицу forms при инициализации.

 Тесты проверяют:
 - успешное установление соединения;
 - создание таблицы forms;
 - корректность структуры таблицы;
 - стабильность при повторной инициализации базы.
 */
class DatabaseTest {

    Database db;

    /**
     Перед каждым тестом создаётся новый экземпляр Database.

     Это вызывает автоматическую проверку существования таблицы forms
     и её создание при необходимости.
     */
    @BeforeEach
    void setUp() {
        db = new Database();
    }

    /**
     Проверяет, что метод getConnection() возвращает действующее подключение.

     Убедиться, что объект соединения не равен null и находится в открытом состоянии.
     */
    @Test
    void shouldReturnValidConnection() throws SQLException {
        try (Connection conn = db.getConnection()) {
            Assertions.assertNotNull(conn);
            Assertions.assertFalse(conn.isClosed());
        }
    }

    /**
     Проверяет, что при создании экземпляра Database таблица forms создаётся автоматически.

     Используется метаданные базы данных для поиска таблицы с именем "forms".
     */
    @Test
    void shouldCreateFormsTableIfNotExists() throws SQLException {
        try (Connection conn = db.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "forms", null)) {
                Assertions.assertTrue(rs.next(), "Таблица forms должна существовать");
            }
        }
    }

    /**
     Проверяет наличие всех ожидаемых столбцов в таблице forms.

     Сравниваются имена колонок, полученные из метаданных, с ожидаемыми:
     chat_id, name, relation, occasion, age, hobbies, budget.
     */
    @Test
    void shouldHaveExpectedColumnsInFormsTable() throws SQLException {
        try (Connection conn = db.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, "forms", null)) {

            StringBuilder columns = new StringBuilder();
            while (rs.next()) {
                columns.append(rs.getString("COLUMN_NAME")).append(",");
            }

            String colNames = columns.toString();
            Assertions.assertTrue(colNames.contains("chat_id"));
            Assertions.assertTrue(colNames.contains("name"));
            Assertions.assertTrue(colNames.contains("relation"));
            Assertions.assertTrue(colNames.contains("occasion"));
            Assertions.assertTrue(colNames.contains("age"));
            Assertions.assertTrue(colNames.contains("hobbies"));
            Assertions.assertTrue(colNames.contains("budget"));
        }
    }

    /**
     Проверяет, что повторное создание экземпляра Database
     не вызывает исключений при повторном вызове CREATE TABLE IF NOT EXISTS.
     */
    @Test
    void shouldNotThrowOnRepeatedInitialization() {
        Assertions.assertDoesNotThrow(Database::new);
    }
}
