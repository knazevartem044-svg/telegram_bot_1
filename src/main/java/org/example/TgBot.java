package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;


import java.util.List;

/**
 * Адаптер к Telegram API: получает апдейты, делегирует их {@link BotLogic}
 * и отправляет текстовые ответы. При /start показывает клавиатуру-меню.
 */
public class TgBot {
    /** Клиент Telegram API. */
    private final TelegramBot bot;
    /** Чистая логика. */
    private final BotLogic logic;

    /** Создаёт бота с новой логикой. */
    public TgBot(String token) {
        this.bot = new TelegramBot(token);
        this.logic = new BotLogic();
    }

    /** Создаёт бота с переданной логикой (удобно для тестов/DI). */
    public TgBot(String token, BotLogic logic) {
        this.bot = new TelegramBot(token);
        this.logic = logic;
    }

    /** Запуск прослушивания апдейтов. */
    public void start() {
        bot.setUpdatesListener(this::onUpdates);
        System.out.println("Бот запущен!");
    }

    /** Обработка пачки апдейтов. */
    private int onUpdates(List<Update> updates) {
        for (Update u : updates) {
            if (u.message() == null || u.message().text() == null) continue;

            long chatId = u.message().chat().id();
            String text = u.message().text();

            Response resp = logic.createResponse(chatId, text);
            if (resp == null) continue;

            SendMessage msg = new SendMessage(resp.getChatId(), resp.getText());

            // При /start BotLogic вернёт флаг showStartMenu=true — прикрепляем клавиатуру
            if (resp.isShowStartMenu()) {
                ReplyKeyboardMarkup startMenu = new ReplyKeyboardMarkup(
                        new String[]{"Начать опрос", "Помощь"},
                        new String[]{"/summary", "/reset"}
                )
                        .resizeKeyboard(true)
                        .oneTimeKeyboard(false);
                msg.replyMarkup(startMenu);
            }


            bot.execute(msg);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
