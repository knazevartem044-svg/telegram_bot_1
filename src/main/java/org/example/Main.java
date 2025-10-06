package org.example;

import io.github.cdimascio.dotenv.Dotenv;
public class Main {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();

        String token = System.getenv("TOKEN_BOT");
        if (token == null || token.isBlank()) {
            token = dotenv.get("TOKEN_BOT");
        }
        TgBot bot = new TgBot(token);
        bot.start();
    }
}
