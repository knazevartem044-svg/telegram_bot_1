package org.example;

/**
 * Класс, представляющий ответ бота без привязки к Telegram API.
 */
public class Response {
    private final long chatId;
    private final String text;

    public Response(long chatId, String text) {
        this.chatId = chatId;
        this.text = text;
    }

    public long getChatId() {
        return chatId;
    }

    public String getText() {
        return text;
    }
}
