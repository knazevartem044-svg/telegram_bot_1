package org.example;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Логика обработки входящих сообщений бота
 */
public class BotLogic {
    /** Порядок шагов анкеты. */
    private enum Step { WHO, OCCASION, AGE, INTERESTS, BUDGET, DONE }

    private static final class Session {
        Step step = Step.WHO;
        String who;
        String occasion;
        String age;
        String interests;
        String budget;
    }
    /** Хранилище сессий по chatId. */
    private final Map<Long, Session> sessions = new HashMap<>();
    /** Текст справки. */
    private static final String HELP = String.join("\n",
            "Привет! Вот список доступных команд:",
            "/start — начать подбор подарка",
            "/reset — сбросить текущую анкету",
            "/summary — показать собранные данные",
            "/help — эта справка"
    );
    public Response createResponse(long chatId, String messageText) {
        if (messageText == null) return null;
        String raw = messageText.trim();
        String msg = raw.toLowerCase(Locale.ROOT);
        // Команды
        switch (msg) {
            case "/start":
                sessions.put(chatId, new Session());
                return new Response(chatId, "Кому подарок?", true);
            case "/help":
                return new Response(chatId, HELP);
            case "/reset": {
                sessions.remove(chatId);
                Session s = new Session();
                sessions.put(chatId, s);
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
            case WHO: {
                s.who = raw;
                s.step = Step.OCCASION;
                return new Response(chatId, "Повод?");
            }
            case OCCASION: {
                s.occasion = raw;
                s.step = Step.AGE;
                return new Response(chatId, "Возраст?");
            }
            case AGE: {
                s.age = raw;
                s.step = Step.INTERESTS;
                return new Response(chatId, "Интересы?");
            }
            case INTERESTS: {
                s.interests = raw;
                s.step = Step.BUDGET;
                return new Response(chatId, "Бюджет?");
            }
            case BUDGET: {
                s.budget = raw;
                s.step = Step.DONE;
                return new Response(chatId, "Отлично! Вот твоя анкета: " + humanSummary(s));
            }
            case DONE: {
                return new Response(chatId, "Анкета уже заполнена: " + humanSummary(s) +
                        "\nИспользуйте /reset, чтобы начать заново, или /summary для просмотра.");
            }
            default:
                return new Response(chatId, "Неизвестное состояние. Попробуйте /reset.");
        }
    }
    /** Читабельное резюме */
    private static String humanSummary(Session s) {
        String whoAcc = s.who == null ? "—" : s.who.toLowerCase(Locale.ROOT);
        String age = s.age == null ? "—" : s.age;
        String interests = s.interests == null ? "—" : s.interests;
        String budget = s.budget == null ? "—" : s.budget;
        return String.format(Locale.ROOT, "%s, %s лет, %s, бюджет %s ₽.", whoAcc, age, interests, budget);
    }
}
