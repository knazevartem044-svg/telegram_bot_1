package org.example.db;

import org.example.model.UserForm;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс FormRepository отвечает за работу с анкетами в базе данных.
 * Сохраняет, получает, удаляет и обновляет данные пользователя.
 * Использует класс Database для подключения к SQLite.
 */
public class FormRepository {

    /** Объект для подключения к базе данных */
    private final Database database;

    /** Конструктор инициализирует подключение к БД */
    public FormRepository() {
        this.database = new Database();
    }

    /**
     * Сохраняет или обновляет анкету в базе данных.
     * Если анкета уже существует, она перезаписывается.
     */
    public void upsert(UserForm f) {
        String sql = """
            INSERT INTO forms (chat_id, name, relation, occasion, age, hobbies, budget)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(chat_id, name) DO UPDATE SET
                relation=excluded.relation,
                occasion=excluded.occasion,
                age=excluded.age,
                hobbies=excluded.hobbies,
                budget=excluded.budget
        """;
        try (Connection c = database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, f.chatId);
            ps.setString(2, f.name);
            ps.setString(3, f.relation);
            ps.setString(4, f.occasion);
            ps.setObject(5, f.age);
            ps.setString(6, f.hobbies);
            ps.setObject(7, f.budget);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении анкеты", e);
        }
    }

    /**
     * Возвращает анкету по имени и ID пользователя.
     * Если анкета не найдена, возвращает null.
     */
    public UserForm get(long chatId, String name) {
        String sql = "SELECT * FROM forms WHERE chat_id = ? AND name = ?";
        try (Connection c = database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new UserForm(
                        rs.getLong("chat_id"),
                        rs.getString("name"),
                        rs.getString("relation"),
                        rs.getString("occasion"),
                        rs.getInt("age"),
                        rs.getString("hobbies"),
                        rs.getInt("budget")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении анкеты", e);
        }
        return null;
    }

    /**
     * Возвращает список всех имён анкет пользователя.
     * Используется для вывода кнопок выбора анкеты.
     */
    public List<String> listNames(long chatId) {
        List<String> result = new ArrayList<>();
        String sql = "SELECT name FROM forms WHERE chat_id = ?";
        try (Connection c = database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка анкет", e);
        }
        return result;
    }

    /**
     * Удаляет анкету пользователя по имени.
     * Используется при нажатии кнопки 🗑 Удалить.
     */
    public void delete(long chatId, String name) {
        String sql = "DELETE FROM forms WHERE chat_id = ? AND name = ?";
        try (Connection c = database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении анкеты", e);
        }
    }
}
