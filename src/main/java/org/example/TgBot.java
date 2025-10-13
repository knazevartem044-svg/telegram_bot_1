package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;

/**
 * Telegram-адаптер: общается с Telegram и маппит Response ↔ Telegram-типы.
 * Логика вынесена в {@link BotLogic} и возвращает {@link Response}.
 */
public class TgBot {

    /** Клиент Telegram Bot API. */
    private final TelegramBot bot;

    /** Чистая логика без зависимостей от Telegram. */
    private final BotLogic logic;

    public TgBot(String token) {
        this.bot = new TelegramBot(token);
        this.logic = new BotLogic();
    }

    public TgBot(String token, BotLogic logic) {
        this.bot = new TelegramBot(token);
        this.logic = logic;
    }

    /** Запуск прослушивания апдейтов. */
    public void start() {
        bot.setUpdatesListener(this::onUpdates);
        System.out.println("Бот запущен!");
    }

    /** Обработка входящих апдейтов от Telegram. */
    private int onUpdates(List<Update> updates) {
        for (Update u : updates) {
            if (u.message() == null || u.message().text() == null) continue;

            long chatId = u.message().chat().id();
            String text = u.message().text();

            // 1) Чистая логика → Response
            Response resp = logic.createResponse(chatId, text);
            if (resp == null) continue;

            // 2) Маппинг Response → SendMessage только здесь, в адаптере
            SendMessage tgMsg = new SendMessage(resp.getChatId(), resp.getText());
            bot.execute(tgMsg);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    /**
     * Прокси к логике, но тип возвращаем как раньше (SendMessage),
     * чтобы НЕ ломать твои текущие тесты.
     */
    public SendMessage createResponse(long chatId, String messageText) {
        Response resp = logic.createResponse(chatId, messageText);
        if (resp == null) return null;
        return new SendMessage(resp.getChatId(), resp.getText());
    }
    // Внутри класса TgBot (org.example.TgBot)
    public String createResponseText(String text) {
        if (text == null || text.isBlank()) {
            return "Введите команду /start или /help.";
        }

        String msg = text.trim().toLowerCase();
        if (msg.equals("/start") || msg.equals("/help")) {
            return "Привет! Вот список доступных команд:\n"
                    + "/start — приветственное сообщение\n"
                    + "/help — справка по командам";
        }
        return text.trim(); // эхо
    }

}
