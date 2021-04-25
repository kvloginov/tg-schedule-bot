package ru.madbunny.schedule.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String BOT_NAME_VAR_NAME = "SCHEDULE_BOT_NAME";
    private static final String BOT_TOKEN_VAR_NAME = "SCHEDULE_BOT_TOKEN";

    private static final Map<String, String> ENV = System.getenv();

    public static void main(String[] args) {
        try {
            var botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new ScheduleBot(ENV.get(BOT_NAME_VAR_NAME), ENV.get(BOT_TOKEN_VAR_NAME)));
            LOGGER.info("Bot registered! Handling started.");
        } catch (TelegramApiException e) {
            LOGGER.error("An error occurred while bot registering", e);
        }
    }
}
