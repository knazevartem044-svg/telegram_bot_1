package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Класс Database управляет подключением к базе данных SQLite.
 * При создании объекта проверяет, существует ли таблица forms, и создаёт её при необходимости.
 * Используется для получения подключений к БД.
 */
public class Database {

    /** Путь к файлу базы данных SQLite */
    private final String url = "jdbc:sqlite:forms.db";

    /** Конструктор создаёт подключение и проверяет наличие таблицы */
    public Database() {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
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
                )""");
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка инициализации базы данных", e);
        }
    }

    /**
     * Возвращает новое подключение к базе данных.
     * Каждый вызов открывает отдельное соединение с SQLite.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
