package org.example;

import okhttp3.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса GiftIdeaService (или, точнее, его поведения при разборе JSON и ошибках).
 */
class GiftIdeaServiceTest {

    @Test
    void shouldReturnParsedGiftIdeaOnSuccess() throws Exception {
        String fakeJson = new JSONObject()
                .put("choices", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("message", new JSONObject()
                                        .put("content", "🎁 Подарок маме"))))
                .toString();

        GiftIdeaService service = new GiftIdeaService() {
            @Override
            public String fetchGiftIdeas(String prompt) throws IOException {
                try (okhttp3.Response r = new okhttp3.Response.Builder()
                        .code(200)
                        .message("OK")
                        .protocol(Protocol.HTTP_1_1)
                        .request(new Request.Builder().url("http://localhost").build())
                        .body(ResponseBody.create(fakeJson, MediaType.get("application/json")))
                        .build()) {

                    String bodyString = r.body().string();
                    JSONObject jsonResp = new JSONObject(bodyString);
                    return jsonResp.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim();
                }
            }
        };

        String result = service.fetchGiftIdeas("подарок для мамы");

        assertEquals("🎁 Подарок маме", result);
    }

    @Test
    void shouldThrowIOExceptionOnErrorCode() {
        GiftIdeaService service = new GiftIdeaService() {
            @Override
            public String fetchGiftIdeas(String prompt) throws IOException {
                throw new IOException("Ошибка от OpenRouter: 500");
            }
        };

        IOException ex = assertThrows(IOException.class,
                () -> service.fetchGiftIdeas("подарок для брата"));

        assertTrue(ex.getMessage().contains("Ошибка от OpenRouter"));
    }

    @Test
    void shouldThrowIOExceptionOnNetworkFailure() {
        GiftIdeaService service = new GiftIdeaService() {
            @Override
            public String fetchGiftIdeas(String prompt) throws IOException {
                throw new IOException("Сетевая ошибка");
            }
        };

        IOException ex = assertThrows(IOException.class,
                () -> service.fetchGiftIdeas("подарок для коллеги"));

        assertTrue(ex.getMessage().contains("Сетевая ошибка"));
    }
}
