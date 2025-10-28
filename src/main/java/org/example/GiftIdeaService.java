package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Сервис, обращающийся к OpenRouter API
 * для генерации идей подарков по анкете пользователя.
 */
public class GiftIdeaService implements GiftIdeaGenerator {

    /**
     * URL эндпоинта OpenRouter API для отправки запросов.
     */
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    /**
     * Имя используемой модели нейросети.
     */
    private static final String MODEL = "gpt-4o-mini";

    /**
     * Название заголовка для авторизации.
     */
    private static final String HEADER_AUTH = "Authorization";

    /**
     * Название заголовка для указания типа содержимого.
     */
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * Название заголовка для ссылки на источник запроса.
     */
    private static final String HEADER_REFERER = "HTTP-Referer";

    /**
     * Название заголовка, указывающего имя приложения.
     */
    private static final String HEADER_TITLE = "X-Title";

    /**
     * MIME-тип тела запроса.
     */
    private static final String CONTENT_TYPE_VALUE = "application/json; charset=utf-8";

    /**
     * Значение заголовка Referer — ссылка на GitHub-аккаунт разработчика.
     */
    private static final String REFERER_VALUE = "https://github.com/stepantsib";

    /**
     * Название клиента, отображаемое в панели OpenRouter.
     */
    private static final String TITLE_VALUE = "Gift Idea Bot";

    /**
     * Системное сообщение, задающее роль нейросети.
     */
    private static final String SYSTEM_PROMPT =
            "Ты помощник, предлагающий креативные идеи подарков. Форматируй красиво и с эмодзи 🎁.";

    /**
     * HTTP-клиент для выполнения запросов.
     */
    private final OkHttpClient client = new OkHttpClient();

    /**
     * API-ключ, загружаемый из .env файла.
     */
    private final String apiKey;

    /**
     * Загружает API-ключ из .env файла.
     */
    public GiftIdeaService() {
        Dotenv dotenv = Dotenv.configure().load();
        this.apiKey = dotenv.get("OPENROUTER_API_KEY");
    }

    /**
     * Отправляет запрос к OpenRouter и возвращает сгенерированные идеи подарков.
     * Если запрос неудачен, выбрасывается IOException.
     */
    @Override
    public String fetchGiftIdeas(String prompt) throws IOException {
        JSONObject json = new JSONObject()
                .put("model", MODEL)
                .put("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "system")
                                .put("content", SYSTEM_PROMPT))
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", prompt)));

        RequestBody body = RequestBody.create(json.toString(),
                MediaType.get(CONTENT_TYPE_VALUE));

        Request request = new Request.Builder()
                .url(API_URL)
                .header(HEADER_AUTH, "Bearer " + apiKey)
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_VALUE)
                .header(HEADER_REFERER, REFERER_VALUE)
                .header(HEADER_TITLE, TITLE_VALUE)
                .post(body)
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
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
