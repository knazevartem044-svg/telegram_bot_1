package org.example;

import java.util.Locale;
public class BotLogic {

    /**
     * Обрабатывает входящий текст и возвращает готовый ответ (Response).
     * Допускается возвращать null если вход пустой/некорректный
     */
    public Response createResponse(long chatId, String messageText) {
        if (messageText == null) return null;

        String raw = messageText.trim();
        String msg = raw.toLowerCase(Locale.ROOT);

        if (msg.equals("/start") || msg.equals("/help")) {
            String help = "Привет! Вот список доступных команд:\n" +
                    "/start — приветственное сообщение\n" +
                    "/help — справка по командам";
            return Response.of(chatId, help);
        }

        if (msg.startsWith("/echo")) {
            String payload = raw.replaceFirst("(?i)^/echo\\s*", "").trim();
            if (payload.isEmpty()) {
                return Response.of(chatId, "Пример: /echo Привет!");
            }
            return Response.of(chatId, payload);
        }

        return Response.of(chatId, "Ты написал << " + messageText + " >>");
    }
}
