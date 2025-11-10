package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.example.telegram.TelegramAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

/**
 * Проверяет, что TgBot корректно обрабатывает обновления и вызывает Telegram API.
 */
class TgBotTest {

    TelegramBot bot;
    TelegramAdapter adapter;
    TgBot tgBot;

    @BeforeEach
    void setUp() {
        bot = Mockito.mock(TelegramBot.class);
        adapter = Mockito.mock(TelegramAdapter.class);

        // Подменяем внутренние поля TgBot через reflection
        tgBot = new TgBot("fake-token") {
            @Override
            public void start() {
                // не запускаем listener
            }

            {
                try {
                    var adapterField = TgBot.class.getDeclaredField("adapter");
                    adapterField.setAccessible(true);
                    adapterField.set(this, adapter);

                    var botField = TgBot.class.getDeclaredField("bot");
                    botField.setAccessible(true);
                    botField.set(this, bot);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    void shouldCallAdapterAndSendMessage() {
        Update upd = Mockito.mock(Update.class);
        Response resp = new Response(12345L, "Привет");

        Mockito.when(adapter.process(upd)).thenReturn(resp);

        try {
            var method = TgBot.class.getDeclaredMethod("onUpdates", List.class);
            method.setAccessible(true);
            method.invoke(tgBot, List.of(upd));
        } catch (Exception e) {
            Assertions.fail(e);
        }

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(bot).execute(captor.capture());

        SendMessage sent = captor.getValue();
        Assertions.assertEquals("Привет", sent.getParameters().get("text"));
        Assertions.assertEquals(12345L, sent.getParameters().get("chat_id"));
    }

    @Test
    void shouldSkipWhenResponseIsNull() {
        Update upd = Mockito.mock(Update.class);
        Mockito.when(adapter.process(upd)).thenReturn(null);

        try {
            var method = TgBot.class.getDeclaredMethod("onUpdates", List.class);
            method.setAccessible(true);
            Object result = method.invoke(tgBot, List.of(upd));

            Assertions.assertEquals(UpdatesListener.CONFIRMED_UPDATES_ALL, result);
            Mockito.verify(bot, Mockito.never()).execute(Mockito.any(SendMessage.class));
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}
