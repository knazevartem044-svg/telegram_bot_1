package org.example.telegram;

import com.pengrad.telegrambot.model.Update;
import org.example.BotLogic;
import org.example.Response;

/**
 * Преобразует Telegram-обновления в понятный формат для логики бота.
 * Это адаптер между Telegram SDK и BotLogic.
 */
public class TelegramAdapter {

    private final BotLogic logic = new BotLogic();

    /**
     * Принимает Telegram Update и передаёт в логику
     */
    public Response process(Update update) {
        long chatId = extractChatId(update);
        String text = extractText(update);
        String callback = extractCallback(update);
        return logic.process(chatId, text, callback);
    }

    private long extractChatId(Update upd) {
        if (upd.message() != null) return upd.message().chat().id();
        if (upd.callbackQuery() != null) return upd.callbackQuery().message().chat().id();
        return -1;
    }

    private String extractText(Update upd) {
        return upd.message() != null ? upd.message().text() : null;
    }

    private String extractCallback(Update upd) {
        return upd.callbackQuery() != null ? upd.callbackQuery().data() : null;
    }
}
