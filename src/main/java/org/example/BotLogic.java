package org.example;

import com.pengrad.telegrambot.request.SendMessage;

import java.util.Locale;

public class BotLogic {

    /** Обрабатывает входящий текст и возвращает готовый ответ. */
    public SendMessage createResponse(long chatId, String messageText) {
        if (messageText == null) return null;

        String msg = messageText.trim().toLowerCase(Locale.ROOT);

        if (msg.equals("/start") || msg.equals("/help")) {
            String help = "Привет! Вот список доступных команд:\n" +
                    "/start — приветственное сообщение\n" +
                    "/help — справка по командам";
            return new SendMessage(chatId, help);
        }

        return new SendMessage(chatId, "Ты написал << " + messageText + " >>");
    }
}
