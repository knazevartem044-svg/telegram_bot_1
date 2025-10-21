package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class TgBotTest {

    private final long chatId = 111L;

    /** /reset до /start — разрешён: возвращает стартовый вопрос. */
    @Test
    void testResetBeforeStartAllowed() {
        BotLogic botLogic = new BotLogic();

        Response r = botLogic.handle(chatId, "/reset");
        Assertions.assertEquals("Анкета сброшена. Кому будем выбирать подарок?", r.getText());

        // После сброса анкета действительно начинается с первого шага
        Response next = botLogic.handle(chatId, "Маме");
        Assertions.assertEquals("Повод?", next.getText());
    }

    /** /reset в середине анкеты — разрешён: прогресс стирается и снова первый шаг. */
    @Test
    void testResetMidSurveyAllowedAndClearsProgress() {
        BotLogic botLogic = new BotLogic();
        botLogic.handle(chatId, "/start");
        botLogic.handle(chatId, "Маме");

        Response reset = botLogic.handle(chatId, "/reset");
        Assertions.assertEquals("Анкета сброшена. Кому будем выбирать подарок?", reset.getText());

        Response after = botLogic.handle(chatId, "Дедушке");
        Assertions.assertEquals("Повод?", after.getText());
    }

    /** /reset после завершения анкеты — разрешён и начинает всё заново. */
    @Test
    void testResetAfterCompletionAllowed() {
        BotLogic botLogic = new BotLogic();
        botLogic.handle(chatId, "/start");
        botLogic.handle(chatId, "Маме");
        botLogic.handle(chatId, "День рождения");
        botLogic.handle(chatId, "45");
        botLogic.handle(chatId, "Кулинария");
        botLogic.handle(chatId, "5000");

        Response reset = botLogic.handle(chatId, "/reset");
        Assertions.assertEquals("Анкета сброшена. Кому будем выбирать подарок?", reset.getText());

        Response r1 = botLogic.handle(chatId, "Папе");
        Assertions.assertEquals("Повод?", r1.getText());
    }

    /** Повторные /reset подряд — идемпотентно: всегда чистое начало. */
    @Test
    void testMultipleResetsIdempotent() {
        BotLogic botLogic = new BotLogic();

        Response r1 = botLogic.handle(chatId, "/reset");
        Assertions.assertEquals("Анкета сброшена. Кому будем выбирать подарок?", r1.getText());

        Response r2 = botLogic.handle(chatId, "/reset");
        Assertions.assertEquals("Анкета сброшена. Кому будем выбирать подарок?", r2.getText());

        Response next = botLogic.handle(chatId, "Сестре");
        Assertions.assertEquals("Повод?", next.getText());
    }

    /** Изоляция по chatId: сброс одного чата не влияет на другой. */
    @Test
    void testResetIsolationByChatId() {
        BotLogic botLogic = new BotLogic();
        long chatA = 100L;
        long chatB = 200L;

        botLogic.handle(chatA, "/start");
        botLogic.handle(chatA, "Маме");
        botLogic.handle(chatB, "/start");
        botLogic.handle(chatB, "Папе");
        Response resetA = botLogic.handle(chatA, "/reset");
        Assertions.assertEquals("Анкета сброшена. Кому будем выбирать подарок?", resetA.getText());
        Response aNext = botLogic.handle(chatA, "Брату");
        Assertions.assertEquals("Повод?", aNext.getText());
        Response bNext = botLogic.handle(chatB, "Юбилей");
        Assertions.assertEquals("Возраст?", bNext.getText());
    }
}
