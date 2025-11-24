package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 Класс Database управляет подключением к SQLite.
 При первом подключении создаёт таблицу forms, если её ещё нет.
 */
class Database {
    /**
     Адрес подключения к базе данных SQLite.
     "jdbc:sqlite:forms.db".
     */
    String url;

    /** Создаёт подключение к основной базе данных */
    Database() {
        this("jdbc:sqlite:forms.db");
    }

    /** Позволяет указать собственный путь к базе (например, для тестов) */
    Database(String url) {
        this.url = url;
        initialize();
    }

    /** Возвращает подключение к базе данных */
    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    /** Создаёт таблицу forms, если она отсутствует */
    void initialize() {
        String sql = """
            CREATE TABLE IF NOT EXISTS forms (
                        chat_id   INTEGER NOT NULL,
                        name      TEXT NOT NULL,
                        relation  TEXT,
                        occasion  TEXT,
                        age       INTEGER,
                        hobbies   TEXT,
                        budget    INTEGER,
                        PRIMARY KEY(chat_id, name)
                    );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
