package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;

/**
 * Telegram-бот, который получает и обрабатывает сообщения.
 */
public class TgBot {

    /** Клиент Telegram Bot API. */
    private final TelegramBot bot;

    /** Логика ответов на сообщения. */
    private final BotLogic logic;

    /**
     * Создаёт бота с токеном и стандартной логикой.
     */
    public TgBot(String token) {
        this(token, new BotLogic());
    }

    /**
     * Создаёт бота с токеном и заданной логикой.
     */
    public TgBot(String token, BotLogic logic) {
        this.bot = new TelegramBot(token);
        this.logic = logic;
    }

    /**
     * Запускает бота и начинает обработку сообщений.
     */
    public void start() {
        bot.setUpdatesListener(this::process);
        System.out.println("Бот запущен!");
    }

    /**
     * Обрабатывает список обновлений от Telegram.
     */
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

    /**
     * Создаёт ответ на сообщение пользователя.
     */
    public SendMessage createResponse(long chatId, String messageText) {
        return logic.createResponse(chatId, messageText);
    }
}
