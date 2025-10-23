package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Отвечает за обращение к нейросети (OpenRouter API)
 * и получение идей подарков на основе анкеты пользователя.
 */
public class GiftIdeaService {
    private final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private final String MODEL = "gpt-4o-mini"; // компактная и быстрая модель
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;

    /**
     * Получает токен (OpenRouter API) из .env файла
     */
    public GiftIdeaService() {
        Dotenv dotenv = Dotenv.configure().load();
        this.apiKey = dotenv.get("OPENROUTER_API_KEY");
    }

    /**
     * Запрашивает идеи подарков на основе анкеты.
     * Создаёт готовый текст запроса для LLM
     * ответ нейросети в виде строки
     * IOException если не удалось обратиться к API
     */
    public String fetchGiftIdeas(String prompt) throws IOException {
        JSONObject json = new JSONObject()
                .put("model", MODEL)
                .put("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "system")
                                .put("content", "Ты помощник, предлагающий креативные идеи подарков. Форматируй красиво и с эмодзи 🎁."))
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", prompt)));

        RequestBody body = RequestBody.create(json.toString(),
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "https://github.com/stepantsib")
                .header("X-Title", "Gift Idea Bot")
                .post(body)
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            {
                if (!response.isSuccessful()) {
                    throw new IOException("Ошибка от OpenRouter: " + response.code());
                }
                String bodyString = response.body().string();
                JSONObject jsonResp = new JSONObject(bodyString);
                return jsonResp.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim();
            }
        }
    }
}
