package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;

/**
 * Класс TgBot отвечает за связь между Telegram и логикой бота.
 * Получает обновления, передаёт их в BotLogic и отправляет пользователю ответы.
 * Сам бот не содержит логики — только транспорт и передача данных.
 */
public class TgBot {

    /** Основной объект TelegramBot для работы с Telegram API */
    private final TelegramBot bot;

    /** Логика бота, которая обрабатывает команды и сообщения */
    private final BotLogic logic = new BotLogic();

    /**
     * Конструктор TgBot.
     * Принимает токен Telegram-бота и создаёт объект для работы с API.
     */
    public TgBot(String token) {
        this.bot = new TelegramBot(token);
    }

    /**
     * Запускает бота.
     * Устанавливает слушатель обновлений и выводит сообщение в консоль.
     */
    public void start() {
        bot.setUpdatesListener(this::onUpdates, Throwable::printStackTrace);
        System.out.println("Bot started...");
    }

    /**
     * Обрабатывает входящие обновления от Telegram.
     * Для каждого update вызывает BotLogic, формирует и отправляет ответ пользователю.
     */
    private int onUpdates(List<Update> updates) {
        for (Update upd : updates) {
            Response resp = logic.processUpdate(upd);
            if (resp == null) continue;

            SendMessage msg = new SendMessage(resp.getChatId(), resp.getText());
            if (resp.getMarkup() != null) {
                msg.replyMarkup(resp.getMarkup());
            }
            bot.execute(msg);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
