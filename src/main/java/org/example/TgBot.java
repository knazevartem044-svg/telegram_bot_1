package org.example;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;

/**
 * тг бот, делегирующий обработку сообщений классу BotLogic.
 */
public class TgBot {

    /** Клиент Telegram API для получения и отправки сообщений. */
    private final TelegramBot bot;

    /** Логика обработки сообщений без зависимостей от тг. */
    private final BotLogic logic;

    /**
     * Создает бота с новым экземпляром логики.
     */
    public TgBot(String token) {
        this.bot = new TelegramBot(token);
        this.logic = new BotLogic();
    }

    /**
     * Создает бота с заданной логикой
     */
    public TgBot(String token, BotLogic logic) {
        this.bot = new TelegramBot(token);
        this.logic = logic;
    }

    /**
     * Запускает получение и обработку апдейтов в тг
     */
    public void start() {
        bot.setUpdatesListener(this::onUpdates);
        System.out.println("Бот запущен!");
    }

    /**
     * Обрабатывает входящие апдейты и отвечает пользователям.
     */
    private int onUpdates(List<Update> updates) {
        for (Update u : updates) {
            if (u.message() == null || u.message().text() == null) continue;

            long chatId = u.message().chat().id();
            String text = u.message().text();
            Response resp = logic.createResponse(chatId, text);
            if (resp == null) continue;

            SendMessage tgMsg = new SendMessage(resp.getChatId(), resp.getText());
            bot.execute(tgMsg);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
