package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты чистой логики BotLogic без Telegram API.
 * Проверяем только преобразование входной строки в Response.
 */
@DisplayName("BotLogic — чистая логика")
public class TgBotTest {

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

    @Test
    @DisplayName("/start -> точная справка")
    void startReturnsHelpExactly() {
        Response r = logic().createResponse(CHAT_ID, "/start");
        assertNotNull(r);
        assertEquals(CHAT_ID, r.getChatId());
        assertEquals(HELP_TEXT, r.getText());
    }

    @Test
    @DisplayName("/help -> точная справка")
    void helpReturnsHelpExactly() {
        Response r = logic().createResponse(CHAT_ID, "/help");
        assertNotNull(r);
        assertEquals(CHAT_ID, r.getChatId());
        assertEquals(HELP_TEXT, r.getText());
    }

    @Test
    @DisplayName("Регистр и пробелы игнорируются для /start и /help")
    void caseAndSpacesIgnoredForCommands() {
        Response r1 = logic().createResponse(CHAT_ID, "   /HeLp   ");
        Response r2 = logic().createResponse(CHAT_ID, "\n/START\t");
        assertEquals(HELP_TEXT, r1.getText());
        assertEquals(HELP_TEXT, r2.getText());
    }

    @Test
    @DisplayName("/echo c текстом -> возвращает полезную нагрузку без подсказок")
    void echoWithPayloadReturnsPayload() {
        Response r = logic().createResponse(CHAT_ID, "/echo   Привет, бот! ");
        assertNotNull(r);
        assertEquals(CHAT_ID, r.getChatId());
        assertEquals("Привет, бот!", r.getText());
    }

    @Test
    @DisplayName("/echo без текста -> подсказка примера")
    void echoWithoutPayloadReturnsExample() {
        assertEquals("Пример: /echo Привет!",
                logic().createResponse(CHAT_ID, "/echo").getText());
        assertEquals("Пример: /echo Привет!",
                logic().createResponse(CHAT_ID, "/echo     ").getText());
    }

    @Test
    @DisplayName("Произвольный текст -> эхо в формате 'Ты написал << ... >>'")
    void arbitraryTextEcho() {
        String input = "Привет, логика!";
        Response r = logic().createResponse(CHAT_ID, input);
        assertEquals("Ты написал << " + input + " >>", r.getText());
        assertEquals(CHAT_ID, r.getChatId());
    }

    @Test
    @DisplayName("Эхо сохраняет исходные пробелы и символы (не триммится)")
    void echoKeepsOriginalWhitespaces() {
        String input = "  42  :)  ";
        Response r = logic().createResponse(CHAT_ID, input);
        assertEquals("Ты написал << " + input + " >>", r.getText());
    }

    @Test
    @DisplayName("null вход -> возвращается null (по контракту BotLogic)")
    void nullInputReturnsNull() {
        assertNull(logic().createResponse(CHAT_ID, null));
    }
}
