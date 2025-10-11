package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;

public class TgBot {
    private final TelegramBot bot;
    private final BotLogic logic;

    public TgBot(String token) {
        this(token, new BotLogic());
    }

    public TgBot(String token, BotLogic logic) {
        this.bot = new TelegramBot(token);
        this.logic = logic;
    }

    public void start() {
        bot.setUpdatesListener(this::process);
        System.out.println("Бот запущен!");
    }

    private int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null || update.message().text() == null) continue;

            long chatId = update.message().chat().id();
            String text = update.message().text();
            SendMessage response = createResponse(chatId, text);
            if (response != null) {
                bot.execute(response);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public SendMessage createResponse(long chatId, String messageText) {
        return logic.createResponse(chatId, messageText);
    }
}
