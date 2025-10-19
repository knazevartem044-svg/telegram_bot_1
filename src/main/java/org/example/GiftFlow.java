package org.example;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Мини-движок диалога подбора подарка.
 * Не зависит от Telegram API. Можно легко мокать в тестах при желании.
 */
public class GiftFlow {

    /** Порядок шагов анкеты. */
    private enum Step { WHO, OCCASION, AGE, INTERESTS, BUDGET, DONE }

    /** Простая сессия пользователя. */
    private static final class Session {
        Step step = Step.WHO;
        String who;
        String occasion;
        String age;
        String interests;
        String budget;
    }

    /** Сессии по chatId. */
    private final Map<Long, Session> sessions = new HashMap<>();

    /** Определяет, должен ли GiftFlow обрабатывать это сообщение. */
    public boolean canHandle(long chatId, String messageText) {
        if (messageText == null) return false;
        String msg = messageText.trim().toLowerCase(Locale.ROOT);
        if (msg.equals("/start") || msg.equals("/reset") || msg.equals("/summary")) return true;

        // Если есть активная сессия — продолжаем шаги до DONE:
        Session s = sessions.get(chatId);
        return s != null && s.step != Step.DONE;
    }

    /** Обрабатывает сообщение. Предполагается вызывать только если canHandle() == true. */
    public Response handle(long chatId, String messageText) {
        String raw = messageText == null ? "" : messageText.trim();
        String msg = raw.toLowerCase(Locale.ROOT);

        switch (msg) {
            case "/start": {
                sessions.put(chatId, new Session());
                // текст вопроса + дальше в TgBot добавим клавиатуру
                return new Response(chatId, "Кому подарок?");
            }
            case "/reset": {
                sessions.remove(chatId);
                sessions.put(chatId, new Session());
                return new Response(chatId, "Анкета сброшена. Кому подарок?");
            }
            case "/summary": {
                Session s = sessions.get(chatId);
                if (s == null) return new Response(chatId, "Анкета пуста. Наберите /start, чтобы начать.");
                return new Response(chatId, "Анкета: " + humanSummary(s));
            }
            default:
        }

        Session s = sessions.computeIfAbsent(chatId, id -> new Session());

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
                return new Response(chatId, "Отлично! Вот твоя анкета: " + humanSummary(s));
            case DONE:
            default:
                return new Response(chatId, "Анкета уже заполнена: " + humanSummary(s) +
                        "\nИспользуйте /reset, чтобы начать заново, или /summary для просмотра.");
        }
    }

    private static String humanSummary(Session s) {
        String whoAcc = s.who == null ? "—" : s.who.toLowerCase(Locale.ROOT);
        String age = s.age == null ? "—" : s.age;
        String interests = s.interests == null ? "—" : s.interests;
        String budget = s.budget == null ? "—" : s.budget;
        return String.format(Locale.ROOT, "%s, %s лет, %s, бюджет %s ₽.", whoAcc, age, interests, budget);
    }
}
