package org.example;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Мини-движок диалога подбора подарка.
 * Управляет пошаговым опросом пользователя и формирует анкету с его ответами.
 * Не зависит от Telegram API, что позволяет легко тестировать его отдельно.
 */
public class GiftFlow {

    /** Перечисление шагов диалога, определяющее порядок вопросов анкеты. */
    private enum Step { WHO, OCCASION, AGE, INTERESTS, BUDGET, DONE }

    /**
     * Класс, представляющий сессию пользователя.
     * Хранит текущее состояние анкеты и все введённые ответы.
     */
    private final class Session {
        /** Текущий шаг заполнения анкеты. */
        Step step = Step.WHO;

        /** Кому предназначен подарок. */
        String who;

        /** Повод для подарка. */
        String occasion;

        /** Возраст получателя подарка. */
        String age;

        /** Интересы получателя. */
        String interests;

        /** Бюджет, выделенный на подарок. */
        String budget;
    }

    /** Коллекция сессий пользователей, сопоставленных по chatId. */
    private final Map<Long, Session> sessions = new HashMap<>();

    /**
     * Определяет, нужно ли GiftFlow обрабатывать текущее сообщение.
     * Возвращает true, если сообщение содержит команду или пользователь находится в процессе заполнения анкеты.
     */
    public boolean canHandle(long chatId, String messageText) {
        if (messageText == null) return false;
        String msg = messageText.trim().toLowerCase(Locale.ROOT);
        if (msg.equals("/start") || msg.equals("/reset") || msg.equals("/summary") || msg.equals("/help"))
            return true;

        Session s = sessions.get(chatId);
        return s != null && s.step != Step.DONE;
    }

    /**
     * Обрабатывает сообщение пользователя, переходя к следующему шагу анкеты или выполняя команды.
     * Игнорирует произвольные тексты, если пользователь не находится в процессе заполнения анкеты.
     */
    public Response handle(long chatId, String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return new Response(chatId, "Сообщение пустое. Попробуйте снова.");
        }

        String raw = messageText.trim();
        String msg = raw.toLowerCase(Locale.ROOT);

        switch (msg) {
            case "/start": {
                Session session = new Session();
                sessions.put(chatId, session);

                String intro = String.join("\n",
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

                return new Response(chatId, intro);
            }
            case "/reset": {
                sessions.remove(chatId);
                sessions.put(chatId, new Session());
                return new Response(chatId, "Анкета сброшена. Кому будем выбирать подарок?");
            }
            case "/summary": {
                Session s = sessions.get(chatId);
                if (s == null)
                    return new Response(chatId, "Анкета пуста. Наберите /start, чтобы начать.");
                return new Response(chatId, "Анкета: \n" + humanSummary(s));
            }
            case "/help": {
                String helpText = String.join("\n",
                        "Доступные команды:",
                        "/start — начать подбор подарка",
                        "/reset — сбросить текущую анкету",
                        "/summary — показать заполненную анкету",
                        "/help — показать список команд"
                );
                return new Response(chatId, helpText);
            }
            default:
        }

        // Проверяем: если пользователь не находится в активной анкете — игнорируем сообщение
        Session s = sessions.get(chatId);
        if (s == null) {
            return new Response(chatId,
                    "Я пока не знаю, что с этим делать 🙂\n" +
                            "Наберите /start, чтобы начать подбор подарка, или /help для списка команд.");
        }

        // Продолжение заполнения анкеты
        switch (s.step) {
            case WHO:
                s.who = raw;
                s.step = Step.OCCASION;
                return new Response(chatId, "Повод?");
            case OCCASION:
                s.occasion = raw;
                s.step = Step.AGE;
                return new Response(chatId, "Возраст?");
            case AGE:
                s.age = raw;
                s.step = Step.INTERESTS;
                return new Response(chatId, "Интересы?");
            case INTERESTS:
                s.interests = raw;
                s.step = Step.BUDGET;
                return new Response(chatId, "Бюджет?");
            case BUDGET:
                s.budget = raw;
                s.step = Step.DONE;
                return new Response(chatId, "Отлично! Вот твоя анкета:\n\n" + humanSummary(s));
            case DONE:
            default:
                return new Response(chatId,
                        "Анкета уже заполнена: " + humanSummary(s)
                                + "\nИспользуйте /reset, чтобы начать заново, или /summary для просмотра.");

        }
    }

    /**
     * Формирует итоговое описание анкеты в человекочитаемом виде.
     * Возвращает структурированный текст с перечислением всех заполненных полей.
     */
    private String humanSummary(Session s) {
        String who = s.who == null ? "—" : s.who;
        String occasion = s.occasion == null ? "—" : s.occasion;
        String age = s.age == null ? "—" : s.age;
        String interests = s.interests == null ? "—" : s.interests;
        String budget = s.budget == null ? "—" : s.budget;

        return String.join("\n",
                "🎁 Твоя анкета:",
                "Кому — " + who,
                "Повод — " + occasion,
                "Возраст — " + age,
                "Интересы — " + interests,
                "Бюджет — " + budget + " ₽"
        );
    }
}
