package org.example;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;

/**
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
            Response resp = logic.createResponse(chatId, text);
            if (resp == null) continue;
            SendMessage tgMsg = new SendMessage(resp.getChatId(), resp.getText());
            bot.execute(tgMsg);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}
