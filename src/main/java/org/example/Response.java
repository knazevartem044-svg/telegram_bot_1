package org.example;

/**
 * Транспорт-независимый ответ логики бота.
 * Логика работает только с этим классом и НЕ знает про Telegram-типы.
 */
public final class Response {
    private final long chatId;
    private final String text;

    private Response(long chatId, String text) {
        this.chatId = chatId;
        this.text = text == null ? "" : text;
    }

    public static Response of(long chatId, String text) {
        return new Response(chatId, text);
    }

    public long getChatId() {
        return chatId;
    }

    public String getText() {
        return text;
    }
}
