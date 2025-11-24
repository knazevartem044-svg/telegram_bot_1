package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.example.telegram.TelegramAdapter;

import java.util.List;

/**
 * Класс TgBot отвечает только за связь между Telegram и логикой.
 * Он не содержит бизнес-логики — только получение и отправку сообщений.
 */
public class TgBot {

    /** Telegram API клиент */
    private final TelegramBot bot;

    /** Адаптер, который преобразует Telegram-обновления в универсальный формат */
    private final TelegramAdapter adapter = new TelegramAdapter();

    /**
     * Конструктор TgBot.
     * Принимает токен Telegram-бота и создаёт объект для работы с API.
     */
    public TgBot(String token) {
        this.bot = new TelegramBot(token);
    }

    /**
     * Запускает бота и слушает обновления.
     */
    public void start() {
        bot.setUpdatesListener(this::onUpdates, Throwable::printStackTrace);
        System.out.println("Bot started...");
    }

    /**
     * Обрабатывает входящие обновления Telegram.
     * Делегирует обработку в BotLogic через TelegramAdapter.
     */
    private int onUpdates(List<Update> updates) {
        for (Update upd : updates) {
            Response resp = adapter.process(upd);
            if (resp == null) continue;

            SendMessage msg = new SendMessage(resp.getChatId(), resp.getText());
            if (resp.getMarkup() != null)
                msg.replyMarkup(resp.getMarkup());

            bot.execute(msg);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
