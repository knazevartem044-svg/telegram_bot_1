package org.example;

/**
 * Интерфейс для генератора идей подарков.
 * Позволяет подставлять разные реализации —
 * реальный сервис с ИИ или тестовую заглушку.
 */
public interface GiftIdeaGenerator {
    /**
     * Возвращает сгенерированные идеи подарков
     * на основе переданного текстового описания (prompt).
     */
    String fetchGiftIdeas(String prompt) throws Exception;
}
