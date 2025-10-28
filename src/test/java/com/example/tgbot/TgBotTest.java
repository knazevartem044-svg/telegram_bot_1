package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для проверки логики класса BotLogic.
 * Проверяются команды, переходы между шагами и игнорирование случайных сообщений.
 */
class BotLogicTest {

    private BotLogic giftFlow;
    private final long chatId = 12345L;

    @BeforeEach
    void setUp() {
        giftFlow = new BotLogic();
    }

    /** Проверяет, что команда /start возвращает точное приветственное сообщение. */
    @Test
    void testStartCommand() {
        Response response = giftFlow.handle(chatId, "/start");

        String expected = String.join("\n",
                "Привет!",
                "Я помогу тебе подобрать подарок всего за несколько шагов.",
                "Чтобы начать скажи, кому будем выбирать подарок?"
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

    /** Проверяет, что команда /reset сбрасывает анкету и возвращает правильный ответ,
     *  а также что анкету действительно очищает. */
    @Test
    void testResetCommand() {
        giftFlow.handle(chatId, "/start");
        giftFlow.handle(chatId, "Маме");
        giftFlow.handle(chatId, "День рождения");
        giftFlow.handle(chatId, "45");
        giftFlow.handle(chatId, "Кулинария");
        giftFlow.handle(chatId, "5000");

        // Выполняем сброс
        Response resetResponse = giftFlow.handle(chatId, "/reset");
        assertEquals("Анкета сброшена. Кому будем выбирать подарок?", resetResponse.getText());

        // Проверяем, что после сброса анкета действительно пуста
        Response summaryAfterReset = giftFlow.handle(chatId, "/summary");

        String expected = String.join("\n",
                "Анкета: \n" +
                        "Твоя анкета:\n" +
                        "Кому — —\n" +
                        "Повод — —\n" +
                        "Возраст — —\n" +
                        "Интересы — —\n" +
                        "Бюджет — — ₽"
        );

        assertEquals(expected, summaryAfterReset.getText());
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
                "Твоя анкета:",
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
                "Твоя анкета:",
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
                "Я пока не знаю, что с этим делать",
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
                        "Твоя анкета:",
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

    /**
     * Проверяет работу команды /ideas с использованием тестовой заглушки.
     * Вместо реального сервиса генерации идей подставляется простая реализация интерфейса GiftIdeaGenerator,
     * возвращающая заранее подготовленный текст.
     *
     * Тест имитирует полный сценарий заполнения анкеты,
     * затем вызывает команду /ideas и проверяет, что ответ совпадает с результатом заглушки.
     */
    @Test
    void testIdeasCommandWithStubbedGenerator() throws Exception {
        // Заглушка: просто возвращает заранее заготовленный текст независимо от prompt
        GiftIdeaGenerator stub = prompt -> String.join("\n",
                "🎁 Идея 1: Фитнес-браслет",
                "🎁 Идея 2: Беспроводные наушники",
                "🎁 Идея 3: Абонемент в спортзал"
        );

        // Внедряем заглушку через тестовый конструктор
        BotLogic logic = new BotLogic(stub);
        long chatId = 2025L;

        // Заполняем анкету до состояния DONE
        logic.handle(chatId, "/start");
        logic.handle(chatId, "Брату");
        logic.handle(chatId, "День рождения");
        logic.handle(chatId, "30");
        logic.handle(chatId, "Спорт, техника");
        logic.handle(chatId, "7000");

        // Команда /ideas должна вернуть ответ заглушки
        Response resp = logic.handle(chatId, "/ideas");

        assertEquals(chatId, resp.getChatId());
        assertTrue(resp.getText().contains("🎁 Идея 1"));
        assertTrue(resp.getText().contains("🎁 Идея 2"));
        assertTrue(resp.getText().contains("🎁 Идея 3"));
    }

}
