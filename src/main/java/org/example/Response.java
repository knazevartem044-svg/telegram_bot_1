package org.example;

/**
 * Ответ логики бота не зависящий от тг апи
 */
public final class Response {

    /** Идентификатор чата, куда отправлять сообщение. */
    private final long chatId;

    /** Текст ответа который нужно отправить. */
    private final String text;

    /**
     * Создает новый объект ответа
     */
    public Response(long chatId, String text) {
        this.chatId = chatId;
        this.text = (text == null ? "" : text);
    }

    /**
     * Возвращает идентификатор чата.
     */
    public long getChatId() {
        return chatId;
    }

    /**
     * Возвращает текст сообщения
     */
    public String getText() {
        return text;
    }
}
