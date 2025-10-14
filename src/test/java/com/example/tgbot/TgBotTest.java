package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты чистой логики BotLogic без Telegram API.
 * Проверяем только преобразование входной строки в Response.
 */

public class TgBotTest {
<<<<<<< Updated upstream

    private static final long CHAT_ID = 123L;

    // Ожидаемый текст справки из BotLogic
    private static final String HELP_TEXT = String.join("\n",
            "Привет! Вот список доступных команд:",
            "/start — приветственное сообщение",
            "/help — справка по командам"
    );

    private BotLogic logic() {
        return new BotLogic();
    }

=======
    private TgBot bot;
    private final long chatId = 111;
    private String expectedHelpText;
    /**
     * Проверка команды /start.
     * Ожидается приветственное сообщение с перечнем команд.
     */
>>>>>>> Stashed changes
    @Test
    void startReturnsHelpExactly() {
        Response r = logic().createResponse(CHAT_ID, "/start");
        assertNotNull(r);
        assertEquals(CHAT_ID, r.getChatId());
        assertEquals(HELP_TEXT, r.getText());
    }

    @Test
    void helpReturnsHelpExactly() {
        Response r = logic().createResponse(CHAT_ID, "/help");
        assertNotNull(r);
        assertEquals(CHAT_ID, r.getChatId());
        assertEquals(HELP_TEXT, r.getText());
    }

    @Test
    void caseAndSpacesIgnoredForCommands() {
        Response r1 = logic().createResponse(CHAT_ID, "   /HeLp   ");
        Response r2 = logic().createResponse(CHAT_ID, "\n/START\t");
        assertEquals(HELP_TEXT, r1.getText());
        assertEquals(HELP_TEXT, r2.getText());
    }



    @Test
    void arbitraryTextEcho() {
        String input = "Привет, логика!";
        Response r = logic().createResponse(CHAT_ID, input);
        assertEquals("Ты написал << " + input + " >>", r.getText());
        assertEquals(CHAT_ID, r.getChatId());
    }

    @Test
    void echoKeepsOriginalWhitespaces() {
        String input = "  42  :)  ";
        Response r = logic().createResponse(CHAT_ID, input);
        assertEquals("Ты написал << " + input + " >>", r.getText());
    }

    @Test
    void nullInputReturnsNull() {
        assertNull(logic().createResponse(CHAT_ID, null));
    }
}
