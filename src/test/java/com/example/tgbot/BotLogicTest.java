package com.example.tgbot;

import org.example.BotLogic;
import org.example.GiftIdeaService;
import org.example.Keyboards;
import org.example.Response;
import org.example.db.FormRepository;
import org.example.model.UserForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

/**
 * Тестовый класс, проверяющий работу основного класса логики BotLogic.
 * Покрывает команды, callback-и, опрос, редактирование анкет
 * и генерацию идей подарков.
 */
class BotLogicTest {

    /** Основной объект логики бота, который тестируется. */
    BotLogic logic;

    /** Поддельный репозиторий анкет для изоляции от базы данных. */
    FormRepository mockRepo;

    /** Поддельный сервис идей подарков для изоляции от внешнего API. */
    GiftIdeaService mockIdeas;

    /** Поддельный генератор клавиатур Telegram для тестов. */
    Keyboards mockKb;

    @BeforeEach
    void init() {
        mockRepo = mock(FormRepository.class);
        mockIdeas = mock(GiftIdeaService.class);
        mockKb = mock(Keyboards.class);

        logic = new BotLogic(mockRepo, mockIdeas, mockKb);
    }


    // ========================
    // Команды
    // ========================

    /** Проверяет, что команда «Помощь» выводит точный текст помощи и вызывает mainReply(). */
    @Test
    void shouldReturnExactHelpText() {
        // Arrange
        String expected = """
                Команды:
                Создать анкету — начать новый опрос
                Мои анкеты — открыть список анкет
                Помощь — показать это сообщение
                """;

        // Act
        Response r = logic.process(1L, "Помощь", null);

        // Assert
        assertNotNull(r);
        assertEquals(expected.strip(), r.getText().strip());
        verify(mockKb).mainReply();
    }

    /** Проверяет реакцию на отсутствие анкет у пользователя. */
    @Test
    void shouldHandleEmptyFormsList() {
        // Arrange
        when(mockRepo.listNames(1L)).thenReturn(List.of());

        // Act
        Response r = logic.process(1L, "Мои анкеты", null);

        // Assert
        assertNotNull(r);
        assertEquals("У вас пока нет анкет. Создайте новую через Создать анкету.", r.getText());
        verify(mockRepo).listNames(1L);
        verify(mockKb).mainReply();
        verify(mockKb, never()).formList(any());
    }

    /** Проверяет, что бот корректно показывает список анкет пользователя. */
    @Test
    void shouldShowFormList() {
        // Arrange
        when(mockRepo.listNames(1L)).thenReturn(List.of("Мама", "Брат"));

        // Act
        Response r = logic.process(1L, "Мои анкеты", null);

        // Assert
        assertNotNull(r);
        assertEquals("Выберите анкету для работы:", r.getText());
        verify(mockRepo).listNames(1L);
        verify(mockKb).formList(List.of("Мама", "Брат"));
    }

    /** Проверяет начало создания новой анкеты через команду. */
    @Test
    void shouldStartFormCreation() {
        // Act
        Response r = logic.process(1L, "Создать анкету", null);

        // Assert
        assertNotNull(r);
        assertEquals("Введите имя новой анкеты.", r.getText());
        // mainReply используется как фон, но можно не проверять клавиатуру здесь
    }

    /** Проверяет реакцию на неизвестную команду. */
    @Test
    void shouldHandleUnknownCommand() {
        // Act
        Response r = logic.process(1L, "Что-то странное", null);

        // Assert
        assertNotNull(r);
        assertEquals("Не понимаю. Используйте /help.", r.getText());
        verify(mockKb).mainReply();
    }

    // ========================
    // Callback (inline кнопки)
    // ========================

    /** Проверяет открытие анкеты при выборе её через inline-кнопку. */
    @Test
    void shouldHandleFormCallback() {
        // Arrange
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        when(mockRepo.get(1L, "Мама")).thenReturn(f);

        String expectedText = """
                Анкета: Мама
                Повод: ДР
                Возраст: 40
                Интересы: сад
                Бюджет: 3000 ₽
                """.strip();

        // Act
        Response r = logic.process(1L, null, "form:Мама");

        // Assert
        assertNotNull(r);
        assertEquals(expectedText, r.getText().strip());
        verify(mockRepo).get(1L, "Мама");
        verify(mockKb).formActions("Мама");
    }

    /** Проверяет открытие несуществующей анкеты. */
    @Test
    void shouldReturnNotFoundWhenFormMissingOnCallback() {
        // Arrange
        when(mockRepo.get(1L, "Мама")).thenReturn(null);

        // Act
        Response r = logic.process(1L, null, "form:Мама");

        // Assert
        assertNotNull(r);
        assertEquals("Анкета не найдена.", r.getText());
        verify(mockRepo).get(1L, "Мама");
        verify(mockKb, never()).formActions(anyString());
    }

    /** Проверяет, что бот предлагает меню редактирования анкеты. */
    @Test
    void shouldHandleEditCallback() {
        // Act
        Response r = logic.process(1L, null, "edit:Мама");

        // Assert
        assertNotNull(r);
        assertEquals("Что хотите изменить в анкете Мама?", r.getText());
        verify(mockKb).editFieldMenu("Мама");
    }

    /** Проверяет запрос подтверждения удаления анкеты. */
    @Test
    void shouldHandleDeleteConfirmation() {
        // Act
        Response r = logic.process(1L, null, "delete:Мама");

        // Assert
        assertNotNull(r);
        assertEquals("Удалить анкету Мама?", r.getText());
        verify(mockKb).confirmDelete("Мама");
    }

    /** Проверяет удаление анкеты после подтверждения. */
    @Test
    void shouldHandleDeleteOk() {
        // Act
        Response r = logic.process(1L, null, "deleteok:Мама");

        // Assert
        assertNotNull(r);
        assertEquals("Анкета Мама удалена.", r.getText());
        verify(mockRepo).delete(1L, "Мама");
        verify(mockKb).mainReply();
    }

    /** Проверяет реакцию на callback forms:list при отсутствии анкет. */
    @Test
    void shouldHandleFormsListCallbackWhenEmpty() {
        // Arrange
        when(mockRepo.listNames(1L)).thenReturn(List.of());

        // Act
        Response r = logic.process(1L, null, "forms:list");

        // Assert
        assertNotNull(r);
        assertEquals("У вас пока нет анкет.", r.getText());
        verify(mockRepo).listNames(1L);
        verify(mockKb).mainReply();
        verify(mockKb, never()).formList(any());
    }

    /** Проверяет callback forms:list, когда анкеты есть. */
    @Test
    void shouldHandleFormsListCallbackWithNames() {
        // Arrange
        when(mockRepo.listNames(1L)).thenReturn(List.of("Мама", "Папа"));

        // Act
        Response r = logic.process(1L, null, "forms:list");

        // Assert
        assertNotNull(r);
        assertEquals("Выберите анкету:", r.getText());
        verify(mockRepo).listNames(1L);
        verify(mockKb).formList(List.of("Мама", "Папа"));
    }

    // ========================
    // Генерация идей подарков
    // ========================

    /** Проверяет успешную генерацию идеи подарка через AI-сервис. */
    @Test
    void shouldGenerateGiftIdea() throws Exception {
        // Arrange
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        when(mockRepo.get(1L, "Мама")).thenReturn(f);
        when(mockIdeas.fetchGiftIdeas(anyString())).thenReturn("🎁 Подарок маме");

        // Act
        Response r = logic.process(1L, null, "idea:Мама");

        // Assert
        assertNotNull(r);
        assertEquals("""
                Идея подарка для Мама:
                🎁 Подарок маме
                """.strip(), r.getText().strip());

        // промпт должен содержать данные анкеты
        verify(mockIdeas).fetchGiftIdeas(
                argThat(prompt ->
                        prompt.contains("мама") &&
                                prompt.contains("ДР") &&
                                prompt.contains("40") &&
                                prompt.contains("сад") &&
                                prompt.contains("3000"))
        );
        verify(mockKb).backToForms();
    }

    /** Проверяет корректную обработку ошибки при генерации идеи подарка. */
    @Test
    void shouldHandleIdeaGenerationError() throws Exception {
        // Arrange
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        when(mockRepo.get(1L, "Мама")).thenReturn(f);
        when(mockIdeas.fetchGiftIdeas(anyString())).thenThrow(new RuntimeException("API down"));

        // Act
        Response r = logic.process(1L, null, "idea:Мама");

        // Assert
        assertNotNull(r);
        assertEquals("""
                Идея подарка для Мама:
                Не удалось получить идею. Попробуйте позже.
                """.strip(), r.getText().strip());
        verify(mockIdeas).fetchGiftIdeas(anyString());
        verify(mockKb).backToForms();
    }

    // ========================
    // Пошаговый опрос (Session)
    // ========================

    /** Проверяет корректное прохождение всех шагов создания анкеты. */
    @Test
    void shouldWalkThroughFormCreationSteps() {
        // Старт
        Response r1 = logic.process(1L, "Создать анкету", null);
        assertEquals("Введите имя новой анкеты.", r1.getText());

        // Имя анкеты
        Response r2 = logic.process(1L, "Мама", null);
        assertEquals("Кому предназначен подарок?", r2.getText());

        // WHO
        Response r3 = logic.process(1L, "мама", null);
        assertEquals("Повод?", r3.getText());

        // REASON
        Response r4 = logic.process(1L, "ДР", null);
        assertEquals("Возраст?", r4.getText());

        // AGE
        Response r5 = logic.process(1L, "45", null);
        assertEquals("Интересы?", r5.getText());

        // HOBBIES
        Response r6 = logic.process(1L, "сад", null);
        assertEquals("Бюджет?", r6.getText());

        // BUDGET + сохранение
        Response r7 = logic.process(1L, "3000", null);
        assertEquals("Анкета Мама сохранена!\nИспользуйте /forms для просмотра.", r7.getText());

        // вот правильная проверка
        verify(mockRepo).upsert(any(UserForm.class));
        verify(mockKb, times(2)).mainReply(); // <— исправлено
    }


    /** Проверяет отклонение некорректного возраста во время опроса. */
    @Test
    void shouldRejectInvalidAgeDuringSurvey() {
        logic.process(1L, "Создать анкету", null);
        logic.process(1L, "Мама", null);
        logic.process(1L, "мама", null);
        logic.process(1L, "ДР", null);

        Response r = logic.process(1L, "abc", null);

        assertEquals("Введите число для возраста.", r.getText());
        verify(mockRepo, never()).upsert(any());
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

        assertEquals("Введите число для бюджета.", r.getText());
        verify(mockRepo, never()).upsert(any());
    }

    // ========================
    // Редактирование анкет
    // ========================

    /** Проверяет успешное изменение возраста анкеты и её сохранение. */
    @Test
    void shouldEditAgeFieldCorrectly() {
        // Arrange
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        when(mockRepo.get(1L, "Мама")).thenReturn(f);

        // Act: выбор поля age и ввод нового значения
        logic.process(1L, null, "editfield:Мама:age");
        Response r = logic.process(1L, "45", null);

        // Assert
        assertNotNull(r);
        assertTrue(r.getText().startsWith("Обновлено!"));
        assertTrue(r.getText().contains("Возраст: 45"));
        verify(mockRepo).get(1L, "Мама");
        verify(mockRepo).upsert(any(UserForm.class));
        verify(mockKb).formActions("Мама");
    }

    /** Проверяет отклонение некорректного возраста при редактировании анкеты. */
    @Test
    void shouldRejectInvalidAgeDuringEdit() {
        // Arrange
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        when(mockRepo.get(1L, "Мама")).thenReturn(f);

        // Act
        logic.process(1L, null, "editfield:Мама:age");
        Response r = logic.process(1L, "abc", null);

        // Assert
        assertEquals("Возраст должен быть числом.", r.getText());
        verify(mockRepo).get(1L, "Мама");
        verify(mockRepo, never()).upsert(any());
    }

    /** Проверяет отклонение некорректного бюджета при редактировании анкеты. */
    @Test
    void shouldRejectInvalidBudgetDuringEdit() {
        // Arrange
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        when(mockRepo.get(1L, "Мама")).thenReturn(f);

        // Act
        logic.process(1L, null, "editfield:Мама:budget");
        Response r = logic.process(1L, "abc", null);

        // Assert
        assertEquals("Бюджет должен быть числом.", r.getText());
        verify(mockRepo).get(1L, "Мама");
        verify(mockRepo, never()).upsert(any());
    }

    /** Проверяет реакцию на попытку редактировать несуществующую анкету. */
    @Test
    void shouldHandleMissingFormDuringEdit() {
        // Arrange
        when(mockRepo.get(1L, "Мама")).thenReturn(null);

        // Act
        logic.process(1L, null, "editfield:Мама:age");
        Response r = logic.process(1L, "45", null);

        // Assert
        assertEquals("Анкета не найдена.", r.getText());
        verify(mockRepo).get(1L, "Мама");
        verify(mockRepo, never()).upsert(any());
        verify(mockKb).mainReply();
    }
}
