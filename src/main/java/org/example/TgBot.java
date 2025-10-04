package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;

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

                System.out.println("Получено сообщение: " + messageText);

                SendMessage response = createResponse(chatId, messageText);
                bot.execute(response);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public SendMessage createResponse(long chatId, String messageText) {
        if (messageText == null) return null;
        String msg = messageText.trim().toLowerCase();

        switch (msg) {
            case "/start":
            case "/help":
                return new SendMessage(chatId,
                        "Привет,z повторяю команды. Напиши мне что-нибудь и я это повторю:\n\n" +
                                "Команды:\n" +
                                "/start - Запустить бота\n" +
                                "/help - Показать информацию про бота");
            default:
                return new SendMessage(chatId, "Ты написал << " + messageText + " >>");
        }
    }
}
