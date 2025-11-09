package org.example.model;

/**
 * Класс UserForm хранит данные одной анкеты пользователя.
 * В анкете содержится информация о человеке, которому нужно подобрать подарок.
 * Эти данные сохраняются в базе и выводятся в чат.
 */
public class UserForm {

    /** Уникальный ID пользователя в Telegram */
    public long chatId;

    /** Название анкеты (например, мама, коллега, брат) */
    public String name;

    /** Кому предназначен подарок (родственник, друг, коллега и т.д.) */
    public String relation;

    /** Повод для подарка (день рождения, праздник и т.п.) */
    public String occasion;

    /** Возраст получателя подарка */
    public Integer age;

    /** Интересы человека, которому дарят подарок */
    public String hobbies;

    /** Сколько пользователь готов потратить на подарок */
    public Integer budget;

    /** Пустой конструктор. Используется при создании пустого объекта. */
    public UserForm() {}

    /**
     * Создаёт новую анкету с заполненными данными.
     * Используется при сохранении информации о получателе подарка.
     */
    public UserForm(long chatId, String name, String relation, String occasion, Integer age, String hobbies, Integer budget) {
        this.chatId = chatId;
        this.name = name;
        this.relation = relation;
        this.occasion = occasion;
        this.age = age;
        this.hobbies = hobbies;
        this.budget = budget;
    }

    /** Возвращает короткий заголовок анкеты (например: “Анкета: мама”) */
    public String prettyCardTitle() {
        return "Анкета: " + name;
    }

    /** Возвращает красиво оформленный текст анкеты для вывода в чат. */
    public String prettyBody() {
        return "Повод: " + (occasion == null ? "-" : occasion) + "\n" +
                "Возраст: " + (age == null ? "-" : age) + "\n" +
                "Интересы: " + (hobbies == null ? "-" : hobbies) + "\n" +
                "Бюджет: " + (budget == null ? "-" : (budget + " ₽"));
    }
}
