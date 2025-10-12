# Telegram Echo Bot (Java, Maven)

Простой Telegram-бот на Java с использованием библиотеки [java-telegram-bot-api](https://github.com/pengrad/java-telegram-bot-api).  
Бот отвечает на команды `/start` и `/help`, а на остальные сообщения возвращает эхо-ответ.

---
## 🧩 Структура проекта

```
telegram_bot_1/
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/org/example/
│   │       ├── Main.java          # Точка входа: загрузка токена и запуск бота
│   │       └── TgBot.java         # Класс Telegram-бота, обработка команд и сообщений
|   |       └── BotLogic.java
│   └── test/
│       └── java/com/example/tgbot/
│           └── TgBotTest.java     # Unit-тесты для метода createResponse
└── .env                           # (опционально) хранение токена
```

---

## 🔧 Пример `.env`

```env
TOKEN_BOT=123456789:ABCDEF...
```
---

## 🧑‍💻 Авторы

- [@knazevartem044-svg](https://github.com/knazevartem044-svg)
- [@stepantsib](https://github.com/stepantsib)

---
