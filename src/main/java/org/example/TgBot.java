package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;
import java.util.Locale;

/**
 * Класс-адаптер для взаимодействия с Telegram API.
 * Обрабатывает входящие сообщения, передавая их в GiftFlow, и отправляет ответы пользователю.
 */
public class TgBot {

    /** Экземпляр Telegram-бота, используемый для получения обновлений и отправки сообщений. */
    private final TelegramBot bot;

    /** Логика диалога для подбора подарков. */
    private final GiftFlow giftFlow;

    /**
     * Создаёт экземпляр Telegram-бота с указанным токеном.
     * Инициализирует движок GiftFlow для обработки диалогов.
     */
    public TgBot(String token) {
        this.bot = new TelegramBot(token);
        this.giftFlow = new GiftFlow();
    }

    /**
     * Запускает бота, устанавливая слушатель обновлений и выводя сообщение о старте.
     */
    public void start() {
        bot.setUpdatesListener(this::onUpdates);
        System.out.println("Бот запущен!");
    }

    /**
     * Обрабатывает входящие обновления Telegram.
     * Проверяет наличие текста, передаёт его в GiftFlow и отправляет ответ пользователю.
     */
    private int onUpdates(List<Update> updates) {
        for (Update u : updates) {
            if (u.message() == null || u.message().text() == null) continue;

            long chatId = u.message().chat().id();
            String userText = u.message().text();
            String lower = userText.trim().toLowerCase(Locale.ROOT);

            Response resp;

            resp = giftFlow.handle(chatId, userText);

            if (resp == null) continue;

            SendMessage out = new SendMessage(resp.getChatId(), resp.getText());
            bot.execute(out);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
