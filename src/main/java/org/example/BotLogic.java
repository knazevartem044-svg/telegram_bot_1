package org.example;

import org.example.db.FormRepository;
import org.example.model.UserForm;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс BotLogic отвечает за основную логику бота.
 * Обрабатывает команды, кнопки и опросы.
 * Работает с анкетами, базой данных и нейросетью для подбора подарков.
 */
public class BotLogic {

    /** Хранилище анкет пользователей */
    private final FormRepository forms = new FormRepository();

    /** Сервис генерации идей подарков */
    private final GiftIdeaService ideaService = new GiftIdeaService();

    /** Класс для создания всех клавиатур */
    private final Keyboards keyboards = new Keyboards();

    /** Имя анкеты, которая создаётся */
    private final Map<Long, String> pendingFormName = new HashMap<>();

    /** Сессии пользователей, участвующих в опросе */
    private final Map<Long, Session> sessions = new HashMap<>();

    /** Анкета, которую сейчас редактирует пользователь */
    private final Map<Long, String> editTarget = new HashMap<>();

    /** Поле анкеты, которое редактируется */
    private final Map<Long, String> editField = new HashMap<>();

    /**
     * Получает обновления от Telegram и вызывает нужный метод.
     */
    public Response process(long chatId, String text, String callbackData) {
        if (callbackData != null)
            return handleCallback(chatId, callbackData);
        if (text != null)
            return handleText(chatId, text.trim());
        return null;
    }

    /**
     * Обрабатывает команды и обычные текстовые сообщения.
     */
    private Response handleText(long chatId, String text) {

        // Команда помощи
        if (text.equals("/help") || text.equals("Помощь")) {
            return new Response(chatId, """
        Команды:
        Создать анкету — начать новый опрос
        Мои анкеты — открыть список анкет
        Помощь — показать это сообщение
        """, keyboards.mainReply());

        }

        // Команда — показать список анкет
        if (text.equals("/forms") || text.equals("Мои анкеты")) {
            List<String> names = forms.listNames(chatId);
            if (names.isEmpty()) {
                return new Response(chatId,
                        "У вас пока нет анкет. Создайте новую через Создать анкету.",
                        keyboards.mainReply());
            }
            return new Response(chatId,
                    "Выберите анкету для работы:",
                    keyboards.formList(names));
        }

        // Команда — создать новую анкету
        if (text.equals("/createform") || text.equals("Создать анкету")) {
            pendingFormName.put(chatId, "__await_name__");
            sessions.put(chatId, new Session());
            return new Response(chatId, "Введите имя новой анкеты.", keyboards.mainReply());
        }

        // Если бот ждёт имя анкеты
        if ("__await_name__".equals(pendingFormName.get(chatId))) {
            pendingFormName.put(chatId, text);
            Session s = sessions.get(chatId);
            s.setStep(Step.WHO);
            return new Response(chatId, "Кому предназначен подарок?");
        }

        // Если пользователь редактирует анкету
        if (editField.containsKey(chatId)) {
            return handleEdit(chatId, text);
        }

        // Если пользователь заполняет новую анкету
        Session s = sessions.get(chatId);
        if (s != null && s.getStep() != null) {
            return handleSurvey(chatId, text, s);
        }

        // Если ничего не подошло
        return new Response(chatId, "Не понимаю. Используйте /help.", keyboards.mainReply());
    }

    /**
     * Обрабатывает процесс редактирования анкеты.
     */
    private Response handleEdit(long chatId, String text) {
        String field = editField.remove(chatId);
        String fname = editTarget.remove(chatId);
        UserForm f = forms.get(chatId, fname);
        if (f == null) {
            return new Response(chatId, "Анкета не найдена.", keyboards.mainReply());
        }

        switch (field) {
            case "occasion" -> f.occasion = text;
            case "age" -> {
                try {
                    f.age = Integer.parseInt(text);
                } catch (Exception e) {
                    return new Response(chatId, "Возраст должен быть числом.");
                }
            }
            case "hobbies" -> f.hobbies = text;
            case "budget" -> {
                try {
                    f.budget = Integer.parseInt(text);
                } catch (Exception e) {
                    return new Response(chatId, "Бюджет должен быть числом.");
                }
            }
        }
        forms.upsert(f);
        return new Response(chatId,
                "Обновлено!\n" + f.prettyCardTitle() + "\n" + f.prettyBody(),
                keyboards.formActions(f.name));
    }

    /**
     * Обрабатывает все нажатия inline-кнопок.
     */
    private Response handleCallback(long chatId, String data) {

        if (data.startsWith("form:")) {
            return openForm(chatId, data.substring(5));
        }

        if (data.startsWith("edit:")) {
            String name = data.substring(5);
            return new Response(chatId,
                    "Что хотите изменить в анкете " + name + "?",
                    keyboards.editFieldMenu(name));
        }

        if (data.startsWith("editfield:")) {
            String[] parts = data.split(":", 3);
            String name = parts[1];
            String field = parts[2];
            editTarget.put(chatId, name);
            editField.put(chatId, field);
            return new Response(chatId, "Введите новое значение поля: " + field);
        }

        if (data.startsWith("delete:")) {
            String name = data.substring(7);
            return new Response(chatId,
                    "Удалить анкету " + name + "?",
                    keyboards.confirmDelete(name));
        }

        if (data.startsWith("deleteok:")) {
            String name = data.substring(9);
            forms.delete(chatId, name);
            return new Response(chatId,
                    "Анкета " + name + " удалена.",
                    keyboards.mainReply());
        }

        if (data.startsWith("idea:")) {
            return generateIdea(chatId, data.substring(5));
        }

        if (data.equals("forms:list")) {
            List<String> names = forms.listNames(chatId);
            if (names.isEmpty())
                return new Response(chatId, "У вас пока нет анкет.", keyboards.mainReply());
            return new Response(chatId, "Выберите анкету:", keyboards.formList(names));
        }

        return null;
    }


    /**
     * Открывает выбранную анкету и показывает её содержимое.
     */
    private Response openForm(long chatId, String name) {
        UserForm f = forms.get(chatId, name);
        if (f == null)
            return new Response(chatId, "Анкета не найдена.");
        return new Response(chatId,
                 f.prettyCardTitle() + "\n" + f.prettyBody(),
                keyboards.formActions(name));
    }

    /**
     * Генерирует идею подарка на основе анкеты.
     */
    private Response generateIdea(long chatId, String name) {
        UserForm f = forms.get(chatId, name);
        if (f == null)
            return new Response(chatId, "Анкета не найдена.");
        String prompt = promptFromForm(f);
        String ideas;
        try {
            ideas = ideaService.fetchGiftIdeas(prompt);
        } catch (Exception e) {
            ideas = "Не удалось получить идею. Попробуйте позже.";
        }
        return new Response(chatId,
                "Идея подарка для " + name + ":\n" + ideas,
                keyboards.backToForms());
    }

    /**
     * Обрабатывает пошаговый опрос при создании анкеты.
     */
    private Response handleSurvey(long chatId, String text, Session s) {
        switch (s.getStep()) {
            case WHO -> {
                s.setWho(text);
                s.setStep(Step.REASON);
                return new Response(chatId, "Повод?");
            }
            case REASON -> {
                s.setReason(text);
                s.setStep(Step.AGE);
                return new Response(chatId, "Возраст?");
            }
            case AGE -> {
                try {
                    s.setAge(Integer.parseInt(text));
                } catch (Exception e) {
                    return new Response(chatId, "Введите число для возраста.");
                }
                s.setStep(Step.HOBBIES);
                return new Response(chatId, "Интересы?");
            }
            case HOBBIES -> {
                s.setHobbies(text);
                s.setStep(Step.BUDGET);
                return new Response(chatId, "Бюджет?");
            }
            case BUDGET -> {
                try {
                    s.setBudget(Integer.parseInt(text));
                } catch (Exception e) {
                    return new Response(chatId, "Введите число для бюджета.");
                }
                s.setStep(Step.DONE);
                String fname = pendingFormName.remove(chatId);
                if (fname != null)
                    forms.upsert(new UserForm(chatId, fname, s.getWho(), s.getReason(), s.getAge(), s.getHobbies(), s.getBudget()));
                sessions.remove(chatId);
                return new Response(chatId,
                        "Анкета " + fname + " сохранена!\nИспользуйте /forms для просмотра.",
                        keyboards.mainReply());
            }
        }
        return null;
    }

    /**
     * Формирует промпт для нейросети на основе анкеты.
     */
    private String promptFromForm(UserForm f) {
        return "Кому: " + f.relation + ". Повод: " + f.occasion + ". Возраст: " + f.age +
                ". Интересы: " + f.hobbies + ". Бюджет: " + f.budget + "₽. " +
                "Предложи 5 идей подарков развёрнуто и с эмодзи.";
    }

    /** Этапы заполнения анкеты */
    public enum Step {WHO, REASON, AGE, HOBBIES, BUDGET, DONE}

    /**
     * Класс Session хранит временные ответы пользователя во время опроса.
     * Нужен, чтобы пошагово собирать анкету и не терять состояние между сообщениями.
     */
    public class Session {

        /** Текущий шаг опроса (WHO, REASON, AGE, HOBBIES, BUDGET, DONE) */
        private Step step;

        /** Кому предназначен подарок (например, мама, брат, коллега) */
        private String who;

        /** Повод для подарка (например, день рождения) */
        private String reason;

        /** Интересы человека, которому дарим подарок */
        private String hobbies;

        /** Возраст получателя подарка */
        private Integer age;

        /** Бюджет на подарок в рублях */
        private Integer budget;

        /** Возвращает текущий шаг опроса */
        public Step getStep() { return step; }

        /** Устанавливает текущий шаг опроса */
        public void setStep(Step step) { this.step = step; }

        /** Возвращает значение поля «кому подарок» */
        public String getWho() { return who; }

        /** Устанавливает значение поля «кому подарок» */
        public void setWho(String who) { this.who = who; }

        /** Возвращает повод для подарка */
        public String getReason() { return reason; }

        /** Устанавливает повод для подарка */
        public void setReason(String reason) { this.reason = reason; }

        /** Возвращает интересы получателя */
        public String getHobbies() { return hobbies; }

        /** Устанавливает интересы получателя */
        public void setHobbies(String hobbies) { this.hobbies = hobbies; }

        /** Возвращает возраст получателя */
        public Integer getAge() { return age; }

        /** Устанавливает возраст получателя */
        public void setAge(Integer age) { this.age = age; }

        /** Возвращает бюджет на подарок */
        public Integer getBudget() { return budget; }

        /** Устанавливает бюджет на подарок */
        public void setBudget(Integer budget) { this.budget = budget; }
    }

}