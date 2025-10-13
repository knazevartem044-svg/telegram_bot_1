package com.example.tgbot;

import org.example.TgBot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты метода createResponse класса TgBot.
 * Проверяется логика ответов без участия Telegram API.
 */
@DisplayName("Логика TgBot — без Telegram API")
public class TgBotTest {

    /**
     * Проверка команды /start — должен содержать приветствие и команды.
     */
    @Test
    void testStart() {
        TgBot bot = new TgBot("test");
        String response = bot.createResponseText("/start");

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains("Привет"));
        Assertions.assertTrue(response.contains("/start"));
        Assertions.assertTrue(response.contains("/help"));
    }

    /**
     * Проверка команды /help — должен возвращать справку.
     */
    @Test
    void testHelp() {
        TgBot bot = new TgBot("test");
        String response = bot.createResponseText("/help");

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains("/start"));
        Assertions.assertTrue(response.contains("/help"));
    }

    /**
     * Проверка произвольного текста — бот должен отвечать эхо.
     */
    @Test
    void testEcho() {
        TgBot bot = new TgBot("test");
        String response = bot.createResponseText("Привет, бот!");
        Assertions.assertEquals("Привет, бот!", response);
    }

    /**
     * Проверка null и пустых строк — должно возвращаться дефолтное сообщение.
     */
    @Test
    void testNullAndEmpty() {
        TgBot bot = new TgBot("test");
        Assertions.assertTrue(bot.createResponseText(null).length() > 0);
        Assertions.assertTrue(bot.createResponseText("   ").length() > 0);
    }
}
