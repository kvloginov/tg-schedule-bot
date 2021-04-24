package ru.madbunny.schedule.bot.dto;

public class User {
    private final int id;
    private final int telegramId;
    private final String nickName;

    public User(int id, int telegramId, String nickName) {
        this.id = id;
        this.telegramId = telegramId;
        this.nickName = nickName;
    }

    public int getId() {
        return id;
    }

    public int getTelegramId() {
        return telegramId;
    }

    public String getNickName() {
        return nickName;
    }
}
