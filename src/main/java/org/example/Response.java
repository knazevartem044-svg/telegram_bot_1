
package org.example;

/**
 * Клас представляющий ответ бота.
 * Хранит идентификатор чата и текст сообщения которое нужно отправить пользователю.
 */
public class Response {

    /** Уникальный идентификатор чата для которого предназначен ответ. */
    private final long chatId;

    /** Текст сообщения который бот должен отправить пользователю. */
    private final String text;

    /**
     * Создаёт новый объект ответа с указанным идентификатором чата и текстом сообщения.
     */
    public Response(long chatId, String text) {
        this.chatId = chatId;
        this.text = text;
    }

    /**
     * Возвращает идентификатор чата связанный с данным ответом.
     */
    public long getChatId() {
        return chatId;
    }

    /**
     * Возвращает текст сообщения предназначенного для пользователя.
     */
    public String getText() {
        return text;
    }
}
