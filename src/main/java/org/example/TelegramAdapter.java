package org.example.telegram;

import com.pengrad.telegrambot.model.Update;
import org.example.BotLogic;
import org.example.GiftIdeaService;
import org.example.Keyboards;
import org.example.Response;
import org.example.db.FormRepository;

/** Адаптер между Telegram Update и логикой бота. */
public class TelegramAdapter {

    /** Логика бота для обработки команд и сообщений. */
    private final BotLogic logic = new BotLogic(
            new FormRepository(),
            new GiftIdeaService(),
            new Keyboards()
    );

    /** Обрабатывает обновление Telegram и передаёт в BotLogic. */
    public Response process(Update update) {
        long chatId = extractChatId(update);
        String text = extractText(update);
        String callback = extractCallback(update);
        return logic.process(chatId, text, callback);
    }

    /** Извлекает chatId из сообщения или callback. */
    private long extractChatId(Update upd) {
        if (upd.message() != null) return upd.message().chat().id();
        if (upd.callbackQuery() != null) return upd.callbackQuery().message().chat().id();
        return -1;
    }

    /** Извлекает текст сообщения. */
    private String extractText(Update upd) {
        return upd.message() != null ? upd.message().text() : null;
    }

    /** Извлекает данные callback-кнопки. */
    private String extractCallback(Update upd) {
        return upd.callbackQuery() != null ? upd.callbackQuery().data() : null;
    }
}
