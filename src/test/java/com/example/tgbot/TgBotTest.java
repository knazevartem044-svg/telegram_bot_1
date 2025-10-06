package com.example.tgbot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pengrad.telegrambot.request.SendMessage;

public class TgBotTest {

    private TgBot bot;
    private final long chatId = 111;

    @BeforeEach
    void setUp() {
        // токен-заглушка, сеть не трогаем — тестируем только createResponse(...)
        bot = new TgBot("nulltoken");
    }

    /** /start -> приветственное сообщение с перечнем команд */
    @Test
    void testStart() {
        SendMessage response = bot.createResponse(chatId, "/start");
        Assertions.assertNotNull(response);

        Object text = response.getParameters().get("text");
        Assertions.assertNotNull(text);

        String s = text.toString();
        // Проверяем ключевые части, а не точный текст (вдруг поправишь формулировку)
        Assertions.assertTrue(s.contains("Привет"));
        Assertions.assertTrue(s.contains("/start"));
        Assertions.assertTrue(s.contains("/help"));

        // правильный chat_id
        Assertions.assertEquals(chatId, response.getParameters().get("chat_id"));
    }

    /** /help -> та же памятка */
    @Test
    void testHelp() {
        SendMessage response = bot.createResponse(chatId, "/help");
        Assertions.assertNotNull(response);

        String s = response.getParameters().get("text").toString();
        Assertions.assertTrue(s.contains("Привет"));
        Assertions.assertTrue(s.contains("/start"));
        Assertions.assertTrue(s.contains("/help"));
        Assertions.assertEquals(chatId, response.getParameters().get("chat_id"));
    }

    /** Команды должны работать без учёта регистра */
    @Test
    void testStartCaseInsensitive() {
        SendMessage response = bot.createResponse(chatId, "/START");
        Assertions.assertNotNull(response);

        String s = response.getParameters().get("text").toString();
        Assertions.assertTrue(s.contains("Привет"));
        Assertions.assertTrue(s.contains("/start"));
        Assertions.assertTrue(s.contains("/help"));
    }

    /** Команда /help без учёта регистра */
    @Test
    void testHelpCaseInsensitive() {
        SendMessage response = bot.createResponse(chatId, "/HELp");
        Assertions.assertNotNull(response);

        String s = response.getParameters().get("text").toString();
        Assertions.assertTrue(s.contains("Привет"));
        Assertions.assertTrue(s.contains("/start"));
        Assertions.assertTrue(s.contains("/help"));
    }

    /** Трим пробелов и регистр */
    @Test
    void testCommandTrimAndCase() {
        SendMessage response = bot.createResponse(chatId, "   /Start   ");
        Assertions.assertNotNull(response);

        String s = response.getParameters().get("text").toString();
        Assertions.assertTrue(s.contains("Привет"));
    }

    /** Эхо для любого другого текста */
    @Test
    void testEcho() {
        String userMessage = "aboba";
        SendMessage response = bot.createResponse(chatId, userMessage);
        Assertions.assertNotNull(response);

        Assertions.assertEquals(
                "Ты написал << " + userMessage + " >>",
                response.getParameters().get("text").toString()
        );
        Assertions.assertEquals(chatId, response.getParameters().get("chat_id"));
    }

    /** null -> метод возвращает null (согласно твоей реализации) */
    @Test
    void testNullMessage() {
        SendMessage response = bot.createResponse(chatId, null);
        Assertions.assertNull(response);
    }
}
