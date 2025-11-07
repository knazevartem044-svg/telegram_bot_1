package org.example;

import com.pengrad.telegrambot.model.request.Keyboard;

/**
 * Ответ бота: чат, текст и опциональная разметка (reply/inline клавиатуры).
 */
public class Response {

    private final long chatId;
    private final String text;
    private final Keyboard markup; // <-- добавили

    public Response(long chatId, String text) {
        this(chatId, text, null);
    }

    public Response(long chatId, String text, Keyboard markup) {
        this.chatId = chatId;
        this.text = text;
        this.markup = markup;
    }

    public long getChatId() { return chatId; }
    public String getText() { return text; }
    public Keyboard getMarkup() { return markup; }
}
