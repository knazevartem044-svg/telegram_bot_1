package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

import java.util.List;
import java.util.Locale;

/**
 * Адаптер к Telegram API: сначала пробует GiftFlow (диалог подарка),
 * если он не берёт сообщение — падает обратно в неизменённый BotLogic (эхо/хелп).
 * На /start показывает клавиатуру-меню.
 */
public class TgBot {
    private final TelegramBot bot;
    private final BotLogic logic;     // твоя старая логика — не трогаем
    private final GiftFlow giftFlow;  // новая надстройка

    public TgBot(String token) {
        this.bot = new TelegramBot(token);
        this.logic = new BotLogic();
        this.giftFlow = new GiftFlow();
    }

    public TgBot(String token, BotLogic logic) {
        this.bot = new TelegramBot(token);
        this.logic = logic;
        this.giftFlow = new GiftFlow();
    }

    public void start() {
        bot.setUpdatesListener(this::onUpdates);
        System.out.println("Бот запущен!");
    }

    private int onUpdates(List<Update> updates) {
        for (Update u : updates) {
            if (u.message() == null || u.message().text() == null) continue;

            long chatId = u.message().chat().id();
            String userText = u.message().text();
            String lower = userText.trim().toLowerCase(Locale.ROOT);

            Response resp;

            // 1) Сначала пробуем обработать “подарочный” сценарий
            if (giftFlow.canHandle(chatId, userText)) {
                resp = giftFlow.handle(chatId, userText);
            } else {
                // 2) Иначе — старая логика (эхо/хелп)
                resp = logic.createResponse(chatId, userText);
            }

            if (resp == null) continue;

            SendMessage out = new SendMessage(resp.getChatId(), resp.getText());

            // Показать старт-меню, если пользователь ввёл /start
            if ("/start".equals(lower)) {
                ReplyKeyboardMarkup startMenu = new ReplyKeyboardMarkup(
                        new String[]{"Начать опрос", "Помощь"},
                        new String[]{"/summary", "/reset"}
                ).resizeKeyboard(true).oneTimeKeyboard(false);
                out.replyMarkup(startMenu);
            }

            bot.execute(out);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
