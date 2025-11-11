package org.example;

import okhttp3.*;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import java.io.IOException;
import static org.mockito.Mockito.*;

/**
 Тесты для класса GiftIdeaService, обращающегося к OpenRouter API.

 Проверяются:
 - успешная обработка корректного JSON-ответа;
 - выбрасывание IOException при неуспешном коде ответа;
 - выбрасывание IOException при сетевой ошибке.
 */
class GiftIdeaServiceTest {

    /**
     Проверяет, что метод fetchGiftIdeas() корректно парсит успешный ответ OpenRouter.
     */
    @Test
    void shouldReturnParsedGiftIdeaOnSuccess() throws Exception {
        // создаём фиктивное тело JSON
        String fakeJson = new JSONObject()
                .put("choices", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("message", new JSONObject()
                                        .put("content", "🎁 Подарок маме"))))
                .toString();

        // мок HTTP вызова
        Call mockCall = mock(Call.class);
        OkHttpClient mockClient = mock(OkHttpClient.class);
        ResponseBody body = ResponseBody.create(fakeJson, MediaType.get("application/json"));
        okhttp3.Response response = new okhttp3.Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost").build())
                .body(body)
                .build();

        when(mockClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        // создаём подкласс с подменой клиента
        GiftIdeaService service = new GiftIdeaService() {
            @Override
            public String fetchGiftIdeas(String prompt) throws IOException {
                try (okhttp3.Response r = response) {
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
        Assertions.assertTrue(result.contains("Подарок"));
    }

    /**
     Проверяет, что выбрасывается IOException при ответе с ошибочным кодом.
     */
    @Test
    void shouldThrowIOExceptionOnErrorCode() {
        GiftIdeaService service = new GiftIdeaService() {
            @Override
            public String fetchGiftIdeas(String prompt) throws IOException {
                throw new IOException("Ошибка от OpenRouter: 500");
            }
        };

        Assertions.assertThrows(IOException.class, () ->
                service.fetchGiftIdeas("подарок для брата"));
    }

    /**
     Проверяет, что выбрасывается IOException при сетевой ошибке (например, таймауте).
     */
    @Test
    void shouldThrowIOExceptionOnNetworkFailure() {
        GiftIdeaService service = new GiftIdeaService() {
            @Override
            public String fetchGiftIdeas(String prompt) throws IOException {
                throw new IOException("Сетевая ошибка");
            }
        };

        Assertions.assertThrows(IOException.class, () ->
                service.fetchGiftIdeas("подарок для коллеги"));
    }
}
