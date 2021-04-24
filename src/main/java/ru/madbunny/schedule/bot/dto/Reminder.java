package ru.madbunny.schedule.bot.dto;

import java.time.Instant;

public class Reminder {
    private final int id;

    // UTC Time
    private final Instant creation_time;
    private final Instant reminder_time;

    private final String description;
    private final boolean isCompleted;

    private final int user_id;

    public Reminder(int id, Instant creation_time, Instant reminder_time, String description, boolean isCompleted, int user_id) {
        this.id = id;
        this.creation_time = creation_time;
        this.reminder_time = reminder_time;
        this.description = description;
        this.isCompleted = isCompleted;
        this.user_id = user_id;
    }

    public int getId() {
        return id;
    }

    public Instant getCreation_time() {
        return creation_time;
    }

    public Instant getReminder_time() {
        return reminder_time;
    }

    public String getDescription() {
        return description;
    }

    public int getUser_id() {
        return user_id;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
