package org.example;

import io.github.cdimascio.dotenv.Dotenv;
public class Main {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String token = System.getenv("TOKEN_BOT");
        if (token == null || token.isBlank()) {
            token = dotenv.get("TOKEN_BOT");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(".env");
        }

        TgBot bot = new TgBot(token);
        bot.start();
    }
}