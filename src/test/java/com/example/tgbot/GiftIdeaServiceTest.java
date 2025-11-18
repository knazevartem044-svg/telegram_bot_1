package org.example;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/** Тесты для GiftIdeaService, проверяющие разбор
 * JSON и обработку ошибок при работе с API. */
class GiftIdeaServiceTest {

    /** Успешный ответ от сервера, корректный JSON */
    @Test
    void shouldReturnParsedGiftIdeaOnSuccess() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();

        String fakeJson = """
        {
          "choices": [{
            "message": { "content": "🎁 Подарок маме" }
          }]
        }
        """;

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(fakeJson));

        GiftIdeaService service = new GiftIdeaService() {
            @Override
            protected String apiUrl() {
                return server.url("/chat/completions").toString();
            }
        };

        String result = service.fetchGiftIdeas("подарок для мамы");
        Assertions.assertEquals("🎁 Подарок маме", result);

        server.shutdown();
    }

    /** Ошибка внешнего API: код ответа 500 */
    @Test
    void shouldThrowIOExceptionOnServerError() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();

        server.enqueue(new MockResponse().setResponseCode(500));

        GiftIdeaService service = new GiftIdeaService() {
            @Override
            protected String apiUrl() {
                return server.url("/fail").toString();
            }
        };

        IOException ex = Assertions.assertThrows(IOException.class,
                () -> service.fetchGiftIdeas("test"));

        Assertions.assertTrue(ex.getMessage().contains("500"));

        server.shutdown();
    }

    /** Сетевая ошибка: сервер недоступен */
    @Test
    void shouldThrowIOExceptionOnNetworkFailure() {
        GiftIdeaService service = new GiftIdeaService() {
            @Override
            protected String apiUrl() {
                return "http://localhost:9999/test";
            }
        };

        Assertions.assertThrows(IOException.class,
                () -> service.fetchGiftIdeas("test"));
    }
}
