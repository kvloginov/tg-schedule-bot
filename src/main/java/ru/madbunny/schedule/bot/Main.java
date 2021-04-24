package ru.madbunny.schedule.bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;

public class Main {

    private static final Map<String, String> ENV = System.getenv();

    public static void main(String[] args) {
        try {
            var botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new ScheduleBot(ENV.get("SCHEDULE_BOT_NAME"), ENV.get("SCHEDULE_BOT_TOKEN")));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
