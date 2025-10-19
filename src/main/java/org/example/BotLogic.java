package org.example;

import java.util.Locale;

/**
 * Логика обработки входящих сообщений бота
 */
public class BotLogic {

    /**
     * Обрабатывает сообщение и формирует ответ
     */
    public Response createResponse(long chatId, String messageText) {
        if (messageText == null) return null;

        String raw = messageText.trim();
        String msg = raw.toLowerCase(Locale.ROOT);

        if (msg.equals("/start") || msg.equals("/help")) {
            String help = "Привет! Вот список доступных команд:\n" +
                    "/start — приветственное сообщение\n" +
                    "/help — справка по командам";
            return new Response(chatId, help);
        }

        return new Response(chatId, "Ты написал << " + messageText + " >>");
    }
}