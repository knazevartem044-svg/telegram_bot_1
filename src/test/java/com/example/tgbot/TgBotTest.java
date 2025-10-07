package com.example.tgbot;

import org.example.TgBot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.pengrad.telegrambot.request.SendMessage;

/**
 * Тесты для метода createResponse класса TgBot.
 * Проверяется корректный ответ на команды, регистр, пробелы, эхо и null.
 */
public class TgBotTest {

    private TgBot bot;
    private final long chatId = 111;
    private String expectedHelpText;

    /**
     * Подготовка тестового экземпляра бота.
     * Ожидаемый текст формируется здесь, без использования static.
     */
    @BeforeEach
    void setUp() {
        bot = new TgBot("nulltoken");

        expectedHelpText =
                "Привет! Вот список доступных команд:\n" +
                        "/start — приветственное сообщение\n" +
                        "/help — справка по командам";
    }

    /**
     * Проверка команды /start.
     * Ожидается приветственное сообщение с перечнем команд.
     */
    @Test
    void testStart() {
        SendMessage response = bot.createResponse(chatId, "/start");
        Assertions.assertNotNull(response);

        String s = response.getParameters().get("text").toString();

        Assertions.assertEquals(expectedHelpText, s);
        Assertions.assertEquals(chatId, response.getParameters().get("chat_id"));
    }

    /**
     * Проверка команды /help.
     * Ожидается тот же текст, что и для /start.
     */
    @Test
    void testHelp() {
        SendMessage response = bot.createResponse(chatId, "/help");
        Assertions.assertNotNull(response);

        String s = response.getParameters().get("text").toString();

        Assertions.assertEquals(expectedHelpText, s);
        Assertions.assertEquals(chatId, response.getParameters().get("chat_id"));
    }

    /**
     * Проверка команды /start без учёта регистра.
     */
    @Test
    void testStartCaseInsensitive() {
        SendMessage response = bot.createResponse(chatId, "/START");
        Assertions.assertNotNull(response);

        String s = response.getParameters().get("text").toString();

        Assertions.assertEquals(expectedHelpText, s);
    }

    /**
     * Проверка команды /help без учёта регистра.
     */
    @Test
    void testHelpCaseInsensitive() {
        SendMessage response = bot.createResponse(chatId, "/HeLp");
        Assertions.assertNotNull(response);

        String s = response.getParameters().get("text").toString();

        Assertions.assertEquals(expectedHelpText, s);
    }

    /**
     * Проверка обработки пробелов и регистра в команде.
     */
    @Test
    void testCommandTrimAndCase() {
        SendMessage response = bot.createResponse(chatId, "   /Start   ");
        Assertions.assertNotNull(response);

        String s = response.getParameters().get("text").toString();

        Assertions.assertEquals(expectedHelpText, s);
    }

    /**
     * Проверка эхо-ответа для произвольного текста.
     */
    @Test
    void testEcho() {
        String userMessage = "aboba";
        SendMessage response = bot.createResponse(chatId, userMessage);
        Assertions.assertNotNull(response);

        String expected = "Ты написал << " + userMessage + " >>";

        Assertions.assertEquals(expected, response.getParameters().get("text").toString());
        Assertions.assertEquals(chatId, response.getParameters().get("chat_id"));
    }

    /**
     * Проверка реакции на null.
     */
    @Test
    void testNullMessage() {
        SendMessage response = bot.createResponse(chatId, null);
        Assertions.assertNull(response);
    }
}
