package org.example;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import java.util.List;

/**
 * Класс Keyboards управляет всеми клавиатурами бота.
 * Отвечает за создание кнопок и меню для удобного взаимодействия с пользователем.
 * Используется при работе с анкетами и командами.
 */
public class Keyboards {

    /** Создаёт основную клавиатуру внизу экрана с командами бота. */
    public ReplyKeyboardMarkup mainReply() {
        return new ReplyKeyboardMarkup(
                new String[]{"Помощь", "Мои анкеты", "Создать анкету"}
        )
                .resizeKeyboard(true)
                .selective(true);
    }

    /**
     * Формирует клавиатуру со списком всех анкет пользователя.
     * Каждая кнопка соответствует одной анкете.
     */
    public InlineKeyboardMarkup formList(List<String> names) {
        InlineKeyboardMarkup kb = new InlineKeyboardMarkup();
        for (String n : names) {
            kb.addRow(new InlineKeyboardButton(n).callbackData("form:" + n));
        }
        return kb;
    }

    /**
     * Создаёт клавиатуру с действиями для выбранной анкеты.
     * Включает кнопки редактирования, удаления и генерации идеи.
     */
    public InlineKeyboardMarkup formActions(String name) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("Отредактировать").callbackData("edit:" + name),
                new InlineKeyboardButton("Удалить").callbackData("delete:" + name),
                new InlineKeyboardButton("Сгенерировать идею").callbackData("idea:" + name)
        );
    }

    /**
     * Создаёт меню выбора поля для редактирования.
     * После выбора бот предложит ввести новое значение.
     */
    public InlineKeyboardMarkup editFieldMenu(String name) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("Повод").callbackData("editfield:" + name + ":occasion"),
                new InlineKeyboardButton("Возраст").callbackData("editfield:" + name + ":age"),
                new InlineKeyboardButton("Интересы").callbackData("editfield:" + name + ":hobbies"),
                new InlineKeyboardButton("Бюджет").callbackData("editfield:" + name + ":budget")
        ).addRow(new InlineKeyboardButton("Назад").callbackData("form:" + name));
    }

    /**
     * Создаёт окно подтверждения удаления анкеты.
     * Позволяет пользователю подтвердить или отменить действие.
     */
    public InlineKeyboardMarkup confirmDelete(String name) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("Да, удалить").callbackData("deleteok:" + name),
                new InlineKeyboardButton("Отмена").callbackData("form:" + name)
        );
    }

    /**
     * Создаёт кнопку возврата к списку анкет.
     * Используется после генерации идей подарков.
     */
    public InlineKeyboardMarkup backToForms() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("К анкетам").callbackData("forms:list")
        );
    }
}
