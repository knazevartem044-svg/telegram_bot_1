package org.example;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Управляет пошаговым опросом пользователя и формирует анкету с его ответами.
 */
public class BotLogic {

    /**
     * Интерфейсный генератор идей подарков.
     * Используется для получения ответов от реального сервиса или тестовой заглушки.
     */
    private final GiftIdeaGenerator ideaGenerator;

    /**
     * Конструктор по умолчанию.
     * Создаёт экземпляр BotLogic с генератором идей по умолчанию.
     * В рабочей среде используется GiftIdeaService, который обращается
     * к внешнему API для получения идей подарков.
     */
    public BotLogic() {
        this.ideaGenerator = new GiftIdeaService();
    }

    /**
     * Конструктор для тестирования.
     * Позволяет подставить собственную реализацию интерфейса GiftIdeaGenerator
     * вместо реального сервиса, например простую заглушку.
     */
    public BotLogic(GiftIdeaGenerator ideaGenerator) {
        this.ideaGenerator = ideaGenerator;
    }

    /**
     * Перечисление шагов диалога, определяющее порядок вопросов анкеты.
     */
    private enum Step {WHO, OCCASION, AGE, INTERESTS, BUDGET, DONE}

    /**
     * Класс, представляющий сессию пользователя.
     * Хранит текущее состояние анкеты и все введённые ответы.
     */
    private final class Session {
        /**
         * Текущий шаг заполнения анкеты.
         */
        Step step = Step.WHO;

        /**
         * Кому предназначен подарок.
         */
        String who;

        /**
         * Повод для подарка.
         */
        String occasion;

        /**
         * Возраст получателя подарка.
         */
        String age;

        /**
         * Интересы получателя.
         */
        String interests;

        /**
         * Бюджет, выделенный на подарок.
         */
        String budget;
    }

    /**
     * Коллекция сессий пользователей, сопоставленных по chatId.
     */
    private final Map<Long, Session> sessions = new HashMap<>();


    /**
     * Обрабатывает сообщение пользователя, переходя к следующему шагу анкеты или выполняя команды.
     */
    public Response handle(long chatId, String messageText) {

        String raw = messageText.trim();
        String msg = raw.toLowerCase(Locale.ROOT);

        switch (msg) {
            case "/start": {
                Session session = new Session();
                sessions.put(chatId, session);

                String intro = """
                        Привет!
                        Я помогу тебе подобрать подарок всего за несколько шагов.
                        Чтобы начать скажи, кому будем выбирать подарок?""";

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
                String helpText = """
                        Доступные команды:
                        /start — начать подбор подарка
                        /reset — сбросить текущую анкету
                        /summary — показать заполненную анкету
                        /help — показать список команд""";

                return new Response(chatId, helpText);
            }
            case "/ideas": {
                Session s = sessions.get(chatId);
                if (s == null || s.step != Step.DONE) {
                    return new Response(chatId, "Сначала заполните анкету. Используйте /start, чтобы начать.");
                }

                String prompt = String.format(
                        "Подбери 3-5 идей подарков на основе данных:\n" +
                                "Кому: %s\n" +
                                "Повод: %s\n" +
                                "Возраст: %s\n" +
                                "Интересы: %s\n" +
                                "Бюджет: %s рублей.\n" +
                                "Формат ответа: по пунктам, красиво оформлено с эмодзи 🎁.",
                        s.who, s.occasion, s.age, s.interests, s.budget
                );

                try {
                    String ideas = ideaGenerator.fetchGiftIdeas(prompt);
                    return new Response(chatId, ideas);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new Response(chatId, "Не удалось получить ответ от нейросети. Попробуйте позже или перезапустите бота командой /start.");
                }
            }

            default:
        }
        Session s = sessions.get(chatId);
        if (s == null) {
            return new Response(chatId,
                    "Я пока не знаю, что с этим делать\n" +
                            "Наберите /start, чтобы начать подбор подарка, или /help для списка команд.");
        }

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
     * Формирует итоговое описание анкеты.
     */
    private String humanSummary(Session s) {
        String who = s.who == null ? "—" : s.who;
        String occasion = s.occasion == null ? "—" : s.occasion;
        String age = s.age == null ? "—" : s.age;
        String interests = s.interests == null ? "—" : s.interests;
        String budget = s.budget == null ? "—" : s.budget;

        return String.join("\n",
                "Твоя анкета:",
                "Кому — " + who,
                "Повод — " + occasion,
                "Возраст — " + age,
                "Интересы — " + interests,
                "Бюджет — " + budget + " ₽"
        );
    }
}
