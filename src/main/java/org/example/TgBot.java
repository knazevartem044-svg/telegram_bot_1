package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;

/**
 * Класс Telegram-бота.
 * Обрабатывает команды /start и /help,
 * остальные сообщения возвращает как эхо-ответ.
 */
public class TgBot {
    private final TelegramBot bot;

    public TgBot(String token) {
        bot = new TelegramBot(token);
    }

    public void start() {
        bot.setUpdatesListener(this::process);
        System.out.println("Бот запущен!");
    }

    public int process(List<Update> updates) {
        for (Update u : updates) {
            if (u.message() != null && u.message().text() != null) {
                long chatId = u.message().chat().id();
                String messageText = u.message().text();
                SendMessage response = createResponse(chatId, messageText);
                bot.execute(response);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public SendMessage createResponse(long chatId, String messageText) {
        if (messageText == null) return null;

        String msg = messageText.trim().toLowerCase();

        if (msg.equals("/start") || msg.equals("/help")) {
            String help = "Привет! Вот список доступных команд:\n" +
                    "/start — приветственное сообщение\n" +
                    "/help — справка по командам";
            return new SendMessage(chatId, help);
        }

        return new SendMessage(chatId, "Ты написал << " + messageText + " >>");
    }
}
