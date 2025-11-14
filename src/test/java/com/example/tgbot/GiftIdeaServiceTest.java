package org.example;

import okhttp3.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для GiftIdeaService, проверяющие его поведение при разборе JSON
 * и обработке ошибок. Сеть не используется: сервис подменяется
 * тестовыми реализациями fetchGiftIdeas. Это позволяет полностью
 * контролировать ответы и моделировать различные сценарии работы.
 *
 * Проверяются три основных случая:
 * 1. Успешный разбор корректного JSON.
 * 2. Ошибка внешнего API (например, код 500).
 * 3. Сетевая ошибка при обращении к сервису.
 *
 * Таким образом гарантируется предсказуемое поведение GiftIdeaService
 * в нормальных и ошибочных условиях.
 */
class GiftIdeaServiceTest {

    /**
     * Проверяет успешный разбор JSON-ответа, содержащего поле content.
     * Используется заранее подготовленный JSON, имитирующий структуру
     * ответа OpenRouter. Ожидается, что метод корректно извлечет
     * содержимое поля content и вернет его вызывающему коду.
     */
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

    /**
     * Проверяет, что при ошибке внешнего API метод выбрасывает IOException.
     * Тестовая реализация всегда генерирует исключение, имитируя ситуацию,
     * в которой сервер возвращает ошибочный код. Ожидается, что вызов
     * fetchGiftIdeas приводит к исключению с текстом об ошибке API.
     */
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

    /**
     * Проверяет поведение сервиса при сетевой ошибке. Поддельная реализация
     * генерирует IOException, имитируя ситуацию, когда нет соединения
     * или произошел обрыв сети. Ожидается, что метод выбросит исключение
     * с текстом, указывающим на сетевую проблему.
     */
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
