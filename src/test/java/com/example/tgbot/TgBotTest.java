package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для проверки логики класса GiftFlow.
 * Проверяются команды, переходы между шагами и игнорирование случайных сообщений.
 */
class GiftFlowTest {

    private GiftFlow giftFlow;
    private final long chatId = 12345L;

    @BeforeEach
    void setUp() {
        giftFlow = new GiftFlow();
    }

    /** Проверяет, что команда /start возвращает точное приветственное сообщение. */
    @Test
    void testStartCommand() {
        Response response = giftFlow.handle(chatId, "/start");

        String expected = String.join("\n",
                "Привет! 🎁",
                "Я помогу тебе подобрать подарок — всего за несколько шагов.",
                "",
                "Я задам пару простых вопросов:",
                "1️⃣ Кому ты выбираешь подарок",
                "2️⃣ Повод",
                "3️⃣ Возраст, интересы и бюджет",
                "",
                "А потом покажу итоговую анкету.",
                "",
                "Если ты не знаешь, какие команды доступны, набери /help 💡",
                "",
                "Чтобы начать — скажи, кому будем выбирать подарок?"
        );

        assertEquals(expected, response.getText());
        assertEquals(chatId, response.getChatId());
    }

    /** Проверяет, что команда /help возвращает точный список доступных команд. */
    @Test
    void testHelpCommand() {
        Response response = giftFlow.handle(chatId, "/help");

        String expected = String.join("\n",
                "Доступные команды:",
                "/start — начать подбор подарка",
                "/reset — сбросить текущую анкету",
                "/summary — показать заполненную анкету",
                "/help — показать список команд"
        );

        assertEquals(expected, response.getText());
    }

    /** Проверяет, что команда /reset сбрасывает анкету и возвращает правильный ответ. */
    @Test
    void testResetCommand() {
        giftFlow.handle(chatId, "/start");
        giftFlow.handle(chatId, "Маме");

        Response response = giftFlow.handle(chatId, "/reset");

        assertEquals("Анкета сброшена. Кому будем выбирать подарок?", response.getText());
    }

    /** Проверяет корректный диалог от начала до завершения анкеты. */
    @Test
    void testFullSurveyFlow() {
        giftFlow.handle(chatId, "/start");

        Response r1 = giftFlow.handle(chatId, "Маме");
        assertEquals("Повод?", r1.getText());

        Response r2 = giftFlow.handle(chatId, "День рождения");
        assertEquals("Возраст?", r2.getText());

        Response r3 = giftFlow.handle(chatId, "45");
        assertEquals("Интересы?", r3.getText());

        Response r4 = giftFlow.handle(chatId, "Кулинария");
        assertEquals("Бюджет?", r4.getText());

        Response r5 = giftFlow.handle(chatId, "5000");

        String expected = String.join("\n",
                "Отлично! Вот твоя анкета:",
                "",
                "🎁 Твоя анкета:",
                "Кому — Маме",
                "Повод — День рождения",
                "Возраст — 45",
                "Интересы — Кулинария",
                "Бюджет — 5000 ₽"
        );

        assertEquals(expected, r5.getText());
    }

    /** Проверяет, что команда /summary выводит анкету в правильном формате. */
    @Test
    void testSummaryCommandAfterCompletion() {
        giftFlow.handle(chatId, "/start");
        giftFlow.handle(chatId, "Маме");
        giftFlow.handle(chatId, "День рождения");
        giftFlow.handle(chatId, "45");
        giftFlow.handle(chatId, "Кулинария");
        giftFlow.handle(chatId, "5000");

        Response summary = giftFlow.handle(chatId, "/summary");

        String expected = "Анкета: \n" + String.join("\n",
                "🎁 Твоя анкета:",
                "Кому — Маме",
                "Повод — День рождения",
                "Возраст — 45",
                "Интересы — Кулинария",
                "Бюджет — 5000 ₽"
        );

        assertEquals(expected, summary.getText());
    }

    /** Проверяет, что случайный ввод вне анкеты не сохраняется. */
    @Test
    void testRandomMessageIgnored() {
        Response response = giftFlow.handle(chatId, "Привет");

        String expected = String.join("\n",
                "Я пока не знаю, что с этим делать 🙂",
                "Наберите /start, чтобы начать подбор подарка, или /help для списка команд."
        );

        assertEquals(expected, response.getText());
    }

    /** Проверяет, что при завершённой анкете ввод не меняет данные. */
    @Test
    void testMessageAfterDone() {
        giftFlow.handle(chatId, "/start");
        giftFlow.handle(chatId, "Маме");
        giftFlow.handle(chatId, "День рождения");
        giftFlow.handle(chatId, "45");
        giftFlow.handle(chatId, "Кулинария");
        giftFlow.handle(chatId, "5000");

        Response response = giftFlow.handle(chatId, "ещё текст");

        String expected = String.join("\n",
                "Анкета уже заполнена: " + String.join("\n",
                        "🎁 Твоя анкета:",
                        "Кому — Маме",
                        "Повод — День рождения",
                        "Возраст — 45",
                        "Интересы — Кулинария",
                        "Бюджет — 5000 ₽"
                ),
                "Используйте /reset, чтобы начать заново, или /summary для просмотра."
        );

        assertEquals(expected, response.getText());
    }

    /** Проверяет реакцию на пустые или null-сообщения. */
    @Test
    void testEmptyOrNullMessage() {
        Response r1 = giftFlow.handle(chatId, null);
        Response r2 = giftFlow.handle(chatId, "   ");

        assertEquals("Сообщение пустое. Попробуйте снова.", r1.getText());
        assertEquals("Сообщение пустое. Попробуйте снова.", r2.getText());
    }
}
