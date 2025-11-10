package com.example.tgbot;

import org.example.BotLogic;
import org.example.GiftIdeaService;
import org.example.Keyboards;
import org.example.Response;
import org.example.db.FormRepository;
import org.example.model.UserForm;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import java.util.List;

/**
 Тестовый класс, проверяющий поведение основного класса логики BotLogic.

 Содержит юнит-тесты для всех сценариев:
 - обработка текстовых команд,
 - inline-кнопок (callback data),
 - пошаговое создание анкеты (опрос),
 - редактирование существующих анкет,
 - генерация идей подарков через AI-сервис.

 Для изоляции логики используются моки зависимостей:
 FormRepository, GiftIdeaService, Keyboards.
 */
class BotLogicTest {

    BotLogic logic;
    FormRepository mockRepo;
    GiftIdeaService mockIdeas;
    Keyboards mockKb;

    /**
     Подготавливает тестовую среду перед каждым тестом.

     Создаются моки зависимостей и подменяются поля в BotLogic
     через reflection, чтобы тестировать изолированную логику
     без реальной БД и внешних API.
     */
    @BeforeEach
    void init() {
        mockRepo = Mockito.mock(FormRepository.class);
        mockIdeas = Mockito.mock(GiftIdeaService.class);
        mockKb = Mockito.mock(Keyboards.class);

        logic = new BotLogic() {
            {
                try {
                    var fForms = BotLogic.class.getDeclaredField("forms");
                    fForms.setAccessible(true);
                    fForms.set(this, mockRepo);

                    var fIdeas = BotLogic.class.getDeclaredField("ideaService");
                    fIdeas.setAccessible(true);
                    fIdeas.set(this, mockIdeas);

                    var fKb = BotLogic.class.getDeclaredField("keyboards");
                    fKb.setAccessible(true);
                    fKb.set(this, mockKb);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    // ========================
    // Команды
    // ========================

    /** Проверяет, что команда "Помощь" выводит список доступных команд. */
    @Test
    void shouldReturnHelpText() {
        Response r = logic.process(1L, "Помощь", null);
        Assertions.assertTrue(r.getText().contains("Команды"));
    }

    /** Проверяет реакцию на отсутствие анкет у пользователя. */
    @Test
    void shouldHandleEmptyFormsList() {
        Mockito.when(mockRepo.listNames(1L)).thenReturn(List.of());
        Response r = logic.process(1L, "Мои анкеты", null);
        Assertions.assertTrue(r.getText().contains("У вас пока нет анкет"));
    }

    /** Проверяет, что бот выводит список анкет, если они есть. */
    @Test
    void shouldShowFormList() {
        Mockito.when(mockRepo.listNames(1L)).thenReturn(List.of("Мама", "Брат"));
        Response r = logic.process(1L, "Мои анкеты", null);
        Assertions.assertTrue(r.getText().contains("Выберите анкету"));
    }

    /** Проверяет начало создания новой анкеты. */
    @Test
    void shouldStartFormCreation() {
        Response r = logic.process(1L, "Создать анкету", null);
        Assertions.assertTrue(r.getText().contains("Введите имя"));
    }

    /** Проверяет реакцию на неизвестную команду. */
    @Test
    void shouldHandleUnknownCommand() {
        Response r = logic.process(1L, "Что-то странное", null);
        Assertions.assertTrue(r.getText().contains("Не понимаю"));
    }

    // ========================
    // Callback (inline кнопки)
    // ========================

    /** Проверяет открытие анкеты при выборе через inline-кнопку. */
    @Test
    void shouldHandleFormCallback() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);
        Response r = logic.process(1L, null, "form:Мама");
        Assertions.assertTrue(r.getText().contains("Анкета"));
    }

    /** Проверяет, что бот предлагает меню редактирования анкеты. */
    @Test
    void shouldHandleEditCallback() {
        Response r = logic.process(1L, null, "edit:Мама");
        Assertions.assertTrue(r.getText().contains("Что хотите изменить"));
    }

    /** Проверяет, что бот запрашивает подтверждение удаления анкеты. */
    @Test
    void shouldHandleDeleteConfirmation() {
        Response r = logic.process(1L, null, "delete:Мама");
        Assertions.assertTrue(r.getText().contains("Удалить анкету"));
    }

    /** Проверяет, что после подтверждения анкета удаляется. */
    @Test
    void shouldHandleDeleteOk() {
        Response r = logic.process(1L, null, "deleteok:Мама");
        Mockito.verify(mockRepo).delete(1L, "Мама");
        Assertions.assertTrue(r.getText().contains("удалена"));
    }

    /** Проверяет реакцию на callback с пустым списком анкет. */
    @Test
    void shouldHandleFormsListCallback() {
        Mockito.when(mockRepo.listNames(1L)).thenReturn(List.of());
        Response r = logic.process(1L, null, "forms:list");
        Assertions.assertTrue(r.getText().contains("пока нет анкет"));
    }

    // ========================
    // Генерация идей подарков
    // ========================

    /** Проверяет успешную генерацию идеи подарка через AI-сервис. */
    @Test
    void shouldGenerateGiftIdea() throws Exception {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);
        Mockito.when(mockIdeas.fetchGiftIdeas(Mockito.anyString())).thenReturn("🎁 Подарок");

        Response r = logic.process(1L, null, "idea:Мама");
        Assertions.assertTrue(r.getText().contains("Идея подарка"));
    }

    /** Проверяет обработку ошибки при генерации идеи подарка. */
    @Test
    void shouldHandleIdeaGenerationError() throws Exception {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);
        Mockito.when(mockIdeas.fetchGiftIdeas(Mockito.anyString())).thenThrow(new RuntimeException());

        Response r = logic.process(1L, null, "idea:Мама");
        Assertions.assertTrue(r.getText().contains("Не удалось получить идею"));
    }

    // ========================
    // Пошаговый опрос (Session)
    // ========================

    /** Проверяет корректное прохождение всех шагов создания анкеты. */
    @Test
    void shouldWalkThroughFormCreationSteps() {
        logic.process(1L, "Создать анкету", null);
        logic.process(1L, "Мама", null);

        Response r1 = logic.process(1L, "мама", null);
        Assertions.assertTrue(r1.getText().contains("Повод"));

        Response r2 = logic.process(1L, "ДР", null);
        Assertions.assertTrue(r2.getText().contains("Возраст"));

        Response r3 = logic.process(1L, "45", null);
        Assertions.assertTrue(r3.getText().contains("Интересы"));

        Response r4 = logic.process(1L, "сад", null);
        Assertions.assertTrue(r4.getText().contains("Бюджет"));

        Response r5 = logic.process(1L, "3000", null);
        Assertions.assertTrue(r5.getText().contains("Анкета Мама сохранена"));
    }

    /** Проверяет отклонение некорректного возраста при опросе. */
    @Test
    void shouldRejectInvalidAgeDuringSurvey() {
        logic.process(1L, "Создать анкету", null);
        logic.process(1L, "Мама", null);
        logic.process(1L, "мама", null);
        logic.process(1L, "ДР", null);

        Response r = logic.process(1L, "abc", null);
        Assertions.assertTrue(r.getText().contains("Введите число"));
    }

    /** Проверяет отклонение некорректного бюджета при опросе. */
    @Test
    void shouldRejectInvalidBudgetDuringSurvey() {
        logic.process(1L, "Создать анкету", null);
        logic.process(1L, "Мама", null);
        logic.process(1L, "мама", null);
        logic.process(1L, "ДР", null);
        logic.process(1L, "45", null);
        logic.process(1L, "сад", null);

        Response r = logic.process(1L, "abc", null);
        Assertions.assertTrue(r.getText().contains("Введите число"));
    }

    // ========================
    // Редактирование анкеты
    // ========================

    /** Проверяет успешное изменение возраста и сохранение анкеты. */
    @Test
    void shouldEditAgeFieldCorrectly() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);

        logic.process(1L, null, "editfield:Мама:age");
        Response r = logic.process(1L, "45", null);

        Assertions.assertTrue(r.getText().contains("Обновлено"));
        Mockito.verify(mockRepo).upsert(Mockito.any(UserForm.class));
    }

    /** Проверяет ошибку при вводе некорректного возраста при редактировании. */
    @Test
    void shouldRejectInvalidAgeDuringEdit() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);

        logic.process(1L, null, "editfield:Мама:age");
        Response r = logic.process(1L, "abc", null);

        Assertions.assertTrue(r.getText().contains("Возраст должен быть числом"));
    }

    /** Проверяет ошибку при вводе некорректного бюджета при редактировании. */
    @Test
    void shouldRejectInvalidBudgetDuringEdit() {
        UserForm f = new UserForm(1L, "Мама", "мама", "ДР", 40, "сад", 3000);
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(f);

        logic.process(1L, null, "editfield:Мама:budget");
        Response r = logic.process(1L, "abc", null);

        Assertions.assertTrue(r.getText().contains("Бюджет должен быть числом"));
    }

    /** Проверяет реакцию на попытку редактировать несуществующую анкету. */
    @Test
    void shouldHandleMissingFormDuringEdit() {
        Mockito.when(mockRepo.get(1L, "Мама")).thenReturn(null);

        logic.process(1L, null, "editfield:Мама:age");
        Response r = logic.process(1L, "45", null);

        Assertions.assertTrue(r.getText().contains("Анкета не найдена"));
    }
}
