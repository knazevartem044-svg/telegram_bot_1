package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс Database управляет подключением к SQLite.
 * Поддерживает передачу пути к базе данных для тестов.
 */
public class Database {

    private final String url;

    /** Конструктор по умолчанию — подключение к основной базе */
    public Database() {
        this.url = "jdbc:sqlite:forms.db";
    }

    /** Конструктор для тестов — можно указать собственную базу */
    public Database(String url) {
        this.url = url;
    }

    /** Возвращает подключение к базе данных */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
