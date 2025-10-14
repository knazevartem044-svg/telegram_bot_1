package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестирует BotLogic без использования Telegram API.
 */
public class TgBotTest {

    /** Идентификатор тестового чата. */
    private static final long CHAT_ID = 123L;

    /** Ожидаемый текст справки из BotLogic. */
    private static final String HELP_TEXT = String.join("\n",
            "Привет! Вот список доступных команд:",
            "/start — приветственное сообщение",
            "/help — справка по командам"
    );

    /**
     * Создает новый экземпляр логики для тестов.
     */
    private BotLogic logic() {
        return new BotLogic();
    }

    /**
     * Проверяет, что /start возвращает точный текст справки.
     */
    @Test
    void startReturnsHelpExactly() {
        Response r = logic().createResponse(CHAT_ID, "/start");
        assertNotNull(r);
        assertEquals(CHAT_ID, r.getChatId());
        assertEquals(HELP_TEXT, r.getText());
    }

    /**
     * Проверяет, что /help возвращает текст справки.
     */
    @Test
    void helpReturnsHelpExactly() {
        Response r = logic().createResponse(CHAT_ID, "/help");
        assertNotNull(r);
        assertEquals(CHAT_ID, r.getChatId());
        assertEquals(HELP_TEXT, r.getText());
    }

    /**
     * Проверяет что команды не зависят от регистра и пробелов.
     */
    @Test
    void caseAndSpacesIgnoredForCommands() {
        Response r1 = logic().createResponse(CHAT_ID, "   /HeLp   ");
        Response r2 = logic().createResponse(CHAT_ID, "\n/START\t");
        assertEquals(HELP_TEXT, r1.getText());
        assertEquals(HELP_TEXT, r2.getText());
    }

    /**
     * Проверяет что произвольный текст возвращается как эхо.
     */
    @Test
    void arbitraryTextEcho() {
        String input = "Привет, логика!";
        Response r = logic().createResponse(CHAT_ID, input);
        assertEquals("Ты написал << " + input + " >>", r.getText());
        assertEquals(CHAT_ID, r.getChatId());
    }

    /**
     * Проверяет что сохраняются исходные пробелы в тексте.
     */
    @Test
    void echoKeepsOriginalWhitespaces() {
        String input = "  42  :)  ";
        Response r = logic().createResponse(CHAT_ID, input);
        assertEquals("Ты написал << " + input + " >>", r.getText());
    }

    /**
     * Проверяет что null-ввод возвращает null-ответ.
     */
    @Test
    void nullInputReturnsNull() {
        assertNull(logic().createResponse(CHAT_ID, null));
    }
}
