package com.example.tgbot;

import org.example.BotLogic;
import org.example.GiftIdeaService;
import org.example.Keyboards;
import org.example.Response;
import org.example.db.FormRepository;
import org.example.model.UserForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.util.List;

/**
 * Тестовый класс, проверяющий работу основного класса логики BotLogic.
 * Покрывает команды, callback-и, опрос, редактирование анкет
 * и генерацию идей подарков.
 */
class BotLogicTest {

    /** Основной объект логики бота, который тестируется. */
    BotLogic logic;

    /** мок репозиторий анкет для изоляции от базы данных. */
    FormRepository mockRepo;

    /** мок сервис идей подарков для изоляции от внешнего API. */
    GiftIdeaService mockIdeas;

    /** мок генератор клавиатур Telegram для тестов. */
    Keyboards mockKb;

    @BeforeEach
    void init() {
        mockRepo = Mockito.mock(FormRepository.class);
        mockIdeas = Mockito.mock(GiftIdeaService.class);
        mockKb = Mockito.mock(Keyboards.class);

        logic = new BotLogic(mockRepo, mockIdeas, mockKb);
    }

    /** Проверяет, что команда «Помощь» выводит точный текст помощи и вызывает mainReply(). */
    @Test
    void shouldReturnExactHelpText() {
        String expected = """
                Команды:
                Создать анкету — начать новый опрос
                Мои анкеты — открыть список анкет
                Помощь — показать это сообщение
                """;
        Response r = logic.process(1L, "Помощь", null);
        Assertions.assertNotNull(r);
        Assertions.assertEquals(expected.strip(), r.getText().strip());
        Mockito.verify(mockKb).mainReply();
    }

    /** Проверяет реакцию на отсутствие анкет у пользователя. */
    @Test
    void shouldHandleEmptyFormsList() {
        Mockito.when(mockRepo.listNames(1L)).thenReturn(List.of());
        Response r = logic.process(1L, "Мои анкеты", null);
        Assertions.assertNotNull(r);
        Assertions.assertEquals("У вас пока нет анкет. Создайте новую через Создать анкету.", r.getText());
        Mockito.verify(mockRepo).listNames(1L);
        Mockito.verify(mockKb).mainReply();
        Mockito.verify(mockKb, Mockito.never()).formList(Mockito.any());
    }

    /** Проверяет, что бот корректно показывает список анкет пользователя. */
    @Test
    void shouldShowFormList() {
        Mockito.when(mockRepo.listNames(1L)).thenReturn(List.of("Мама", "Брат"));
        Response r = logic.process(1L, "Мои анкеты", null);
        Assertions.assertNotNull(r);
        Assertions.assertEquals("Выберите анкету для работы:", r.getText());
        Mockito.verify(mockRepo).listNames(1L);
        Mockito.verify(mockKb).formList(List.of("Мама", "Брат"));
    }

    /** Проверяет начало создания новой анкеты через команду. */
    @Test
    void shouldStartFormCreation() {
        Response r = logic.process(1L, "Создать анкету", null);
        Assertions.assertNotNull(r);
        Assertions.assertEquals("Введите имя новой анкеты.", r.getText());

    }

    /** Проверяет реакцию на неизвестную команду. */
    @Test
    void shouldHandleUnknownCommand() {
        Response r = logic.process(1L, "Что-то странное", null);
        Assertions.assertNotNull(r);
        Assertions.assertEquals("Не понимаю. Используйте /help.", r.getText());
        Mockito.verify(mockKb).mainReply();
    }
    /** Проверяет открытие анкеты при выборе её через inline-кнопку. */
    @Test
    void shouldHandleFormCallback() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);

        String expectedText = """
                Анкета: Мама
                Повод: ДР
                Возраст: 40
                Интересы: сад
                Бюджет: 3000 ₽
                """.strip();
        Response r = logic.process(1L, null, "form:Мама");
        Assertions.assertNotNull(r);
        Assertions.assertEquals(expectedText, r.getText().strip());
        Mockito.verify(mockRepo).get(1L, "Мама");
        Mockito.verify(mockKb).formActions("Мама");
    }

    /** Проверяет открытие несуществующей анкеты. */
    @Test
    void shouldReturnNotFoundWhenFormMissingOnCallback() {
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(null);
        Response r = logic.process(1L, null, "form:Мама");
        Assertions.assertNotNull(r);
        Assertions.assertEquals("Анкета не найдена.", r.getText());
        Mockito.verify(mockRepo).get(1L, "Мама");
        Mockito.verify(mockKb, Mockito.never()).formActions(Mockito.anyString());
    }

    /** Проверяет, что бот предлагает меню редактирования анкеты. */
    @Test
    void shouldHandleEditCallback() {
        Response r = logic.process(1L, null, "edit:Мама");
        Assertions.assertNotNull(r);
        Assertions.assertEquals("Что хотите изменить в анкете Мама?", r.getText());
        Mockito.verify(mockKb).editFieldMenu("Мама");
    }

    /** Проверяет запрос подтверждения удаления анкеты. */
    @Test
    void shouldHandleDeleteConfirmation() {
        Response r = logic.process(1L, null, "delete:Мама");
        Assertions.assertNotNull(r);
        Assertions.assertEquals("Удалить анкету Мама?", r.getText());
        Mockito.verify(mockKb).confirmDelete("Мама");
    }

    /** Проверяет удаление анкеты после подтверждения. */
    @Test
    void shouldHandleDeleteOk() {
        Response r = logic.process(1L, null, "deleteok:Мама");
        Assertions.assertNotNull(r);
        Assertions.assertEquals("Анкета Мама удалена.", r.getText());
        Mockito.verify(mockRepo).delete(1L, "Мама");
        Mockito.verify(mockKb).mainReply();
    }

    /** Проверяет реакцию на callback forms:list при отсутствии анкет. */
    @Test
    void shouldHandleFormsListCallbackWhenEmpty() {
        Mockito.when(mockRepo.listNames(1L)).thenReturn(List.of());
        Response r = logic.process(1L, null, "forms:list");
        Assertions.assertNotNull(r);
        Assertions.assertEquals("У вас пока нет анкет.", r.getText());
        Mockito.verify(mockRepo).listNames(1L);
        Mockito.verify(mockKb).mainReply();
        Mockito.verify(mockKb, Mockito.never()).formList(Mockito.any());
    }

    /** Проверяет callback forms:list, когда анкеты есть. */
    @Test
    void shouldHandleFormsListCallbackWithNames() {
        Mockito.when(mockRepo.listNames(1L)).thenReturn(List.of("Мама", "Папа"));
        Response r = logic.process(1L, null, "forms:list");
        Assertions.assertNotNull(r);
        Assertions.assertEquals("Выберите анкету:", r.getText());
        Mockito.verify(mockRepo).listNames(1L);
        Mockito.verify(mockKb).formList(List.of("Мама", "Папа"));
    }
    /** Проверяет успешную генерацию идеи подарка через AI-сервис. */
    @Test
    void shouldGenerateGiftIdea() throws Exception {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);
        Mockito.when(mockIdeas.fetchGiftIdeas(Mockito.anyString())).thenReturn("Подарок маме");
        Response r = logic.process(1L, null, "idea:Мама");
        Assertions.assertNotNull(r);
        Assertions.assertEquals("""
                Идея подарка для Мама:
                Подарок маме
                """.strip(), r.getText().strip());

        // промпт должен содержать данные анкеты
        Mockito.verify(mockIdeas).fetchGiftIdeas(
                Mockito.argThat(prompt ->
                        prompt.contains("мама") &&
                                prompt.contains("ДР") &&
                                prompt.contains("40") &&
                                prompt.contains("сад") &&
                                prompt.contains("3000"))
        );
        Mockito.verify(mockKb).backToForms();
    }

    /** Проверяет корректную обработку ошибки при генерации идеи подарка. */
    @Test
    void shouldHandleIdeaGenerationError() throws Exception {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);
        Mockito.when(mockIdeas.fetchGiftIdeas(Mockito.anyString()))
                .thenThrow(new RuntimeException("API down"));
        Response r = logic.process(1L, null, "idea:Мама");
        Assertions.assertNotNull(r);
        Assertions.assertEquals("""
                Идея подарка для Мама:
                Не удалось получить идею. Попробуйте позже.
                """.strip(), r.getText().strip());
        Mockito.verify(mockIdeas).fetchGiftIdeas(Mockito.anyString());
        Mockito.verify(mockKb).backToForms();
    }

    /** Проверяет корректное прохождение всех шагов создания анкеты. */
    @Test
    void shouldWalkThroughFormCreationSteps() {
        Response r1 = logic.process(1L, "Создать анкету", null);
        Assertions.assertEquals("Введите имя новой анкеты.", r1.getText());

        // Имя анкеты
        Response r2 = logic.process(1L, "Мама", null);
        Assertions.assertEquals("Кому предназначен подарок?", r2.getText());

        // WHO
        Response r3 = logic.process(1L, "мама", null);
        Assertions.assertEquals("Повод?", r3.getText());

        // REASON
        Response r4 = logic.process(1L, "ДР", null);
        Assertions.assertEquals("Возраст?", r4.getText());

        // AGE
        Response r5 = logic.process(1L, "45", null);
        Assertions.assertEquals("Интересы?", r5.getText());

        // HOBBIES
        Response r6 = logic.process(1L, "сад", null);
        Assertions.assertEquals("Бюджет?", r6.getText());

        // BUDGET + сохранение
        Response r7 = logic.process(1L, "3000", null);
        Assertions.assertEquals("Анкета Мама сохранена!\nИспользуйте /forms для просмотра.", r7.getText());
        Mockito.verify(mockRepo).upsert(Mockito.any(UserForm.class));
        Mockito.verify(mockKb, Mockito.times(2)).mainReply(); // <— исправлено
    }

    /** Проверяет отклонение некорректного возраста во время опроса. */
    @Test
    void shouldRejectInvalidAgeDuringSurvey() {
        logic.process(1L, "Создать анкету", null);
        logic.process(1L, "Мама", null);
        logic.process(1L, "мама", null);
        logic.process(1L, "ДР", null);

        Response r = logic.process(1L, "abc", null);

        Assertions.assertEquals("Введите число для возраста.", r.getText());
        Mockito.verify(mockRepo, Mockito.never()).upsert(Mockito.any());
    }

    /** Проверяет отклонение некорректного бюджета во время опроса. */
    @Test
    void shouldRejectInvalidBudgetDuringSurvey() {
        logic.process(1L, "Создать анкету", null);
        logic.process(1L, "Мама", null);
        logic.process(1L, "мама", null);
        logic.process(1L, "ДР", null);
        logic.process(1L, "45", null);
        logic.process(1L, "сад", null);

        Response r = logic.process(1L, "abc", null);

        Assertions.assertEquals("Введите число для бюджета.", r.getText());
        Mockito.verify(mockRepo, Mockito.never()).upsert(Mockito.any());
    }


    /** Проверяет успешное изменение возраста анкеты и её сохранение. */
    @Test
    void shouldEditAgeFieldCorrectly() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);

        logic.process(1L, null, "editfield:Мама:age");
        Response r = logic.process(1L, "45", null);
        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.getText().startsWith("Обновлено!"));
        Assertions.assertTrue(r.getText().contains("Возраст: 45"));
        Mockito.verify(mockRepo).get(1L, "Мама");
        Mockito.verify(mockRepo).upsert(Mockito.any(UserForm.class));
        Mockito.verify(mockKb).formActions("Мама");
    }

    /** Проверяет отклонение некорректного возраста при редактировании анкеты. */
    @Test
    void shouldRejectInvalidAgeDuringEdit() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);
        logic.process(1L, null, "editfield:Мама:age");
        Response r = logic.process(1L, "abc", null);
        Assertions.assertEquals("Возраст должен быть числом.", r.getText());
        Mockito.verify(mockRepo).get(1L, "Мама");
        Mockito.verify(mockRepo, Mockito.never()).upsert(Mockito.any());
    }

    /** Проверяет отклонение некорректного бюджета при редактировании анкеты. */
    @Test
    void shouldRejectInvalidBudgetDuringEdit() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);
        logic.process(1L, null, "editfield:Мама:budget");
        Response r = logic.process(1L, "abc", null);
        Assertions.assertEquals("Бюджет должен быть числом.", r.getText());
        Mockito.verify(mockRepo).get(1L, "Мама");
        Mockito.verify(mockRepo, Mockito.never()).upsert(Mockito.any());
    }

    /** Проверяет реакцию на попытку редактировать несуществующую анкету. */
    @Test
    void shouldHandleMissingFormDuringEdit() {
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(null);
        logic.process(1L, null, "editfield:Мама:age");
        Response r = logic.process(1L, "45", null);
        Assertions.assertEquals("Анкета не найдена.", r.getText());
        Mockito.verify(mockRepo).get(1L, "Мама");
        Mockito.verify(mockRepo, Mockito.never()).upsert(Mockito.any());
        Mockito.verify(mockKb).mainReply();
    }
}
