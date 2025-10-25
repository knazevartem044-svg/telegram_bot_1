# 🎁 Telegram Bot — Gift Idea Assistant

Простой Telegram-бот на **Java**, который помогает пользователям подбирать идеи подарков и общается через Telegram API.

---

## ✨ Возможности

- 📩 Обработка сообщений и команд Telegram  
- 🎁 Генерация идей подарков (через `GiftIdeaService`)  
- 🧠 Гибкая логика ответов (`BotLogic` и `Response`)  
- ⚡ Лёгкий запуск через `Main.java`

---

## 🧩 Структура проекта

```
telegram_bot_1/
├── pom.xml                         # Maven-конфигурация
├── src/
│   ├── main/java/org/example/
│   │   ├── Main.java               # Точка входа
│   │   ├── TgBot.java              # Основной класс Telegram-бота
│   │   ├── BotLogic.java           # Логика обработки сообщений
│   │   ├── GiftIdeaService.java    # Генерация идей подарков
│   │   └── Response.java           # Модель ответа пользователю
│   └── test/java/com/example/tgbot/
│       └── TgBotTest.java          # Тесты
└── .gitignore
```

---

## ⚙️ Как это работает

- `Main` запускает бота (`TgBot`) и регистрирует обновления.  
- `BotLogic` анализирует входящие сообщения.  
- `GiftIdeaService` подбирает идею подарка по интересам пользователя.  
- `Response` формирует текстовый ответ для Telegram API.

---

## 🔑 Пример файла `.env`

```env
TOKEN_BOT=123456789:ABCDEF...
OPENROUTER_API_KEY=sk-...
```

---

## 🧪 Тесты

```bash
mvn test
```

---

## 👥 Авторы

- [@knazevartem044-svg](https://github.com/knazevartem044-svg)
- [@stepantsib](https://github.com/stepantsib)
