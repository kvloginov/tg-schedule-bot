package ru.madbunny.schedule.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String BOT_NAME_VAR_NAME = "SCHEDULE_BOT_NAME";
    private static final String BOT_TOKEN_VAR_NAME = "SCHEDULE_BOT_TOKEN";

    private static final Map<String, String> ENV = System.getenv();

    public static void main(String[] args) {
        try {
            var botsApi = new TelegramBotsApi(DefaultBotSession.class);
            ScheduleBot bot = new ScheduleBot(ENV.get(BOT_NAME_VAR_NAME), ENV.get(BOT_TOKEN_VAR_NAME));
            botsApi.registerBot(bot);
            LOGGER.info("Bot registered! Handling started.");
            initReminderSender(bot);
            LOGGER.info("ReminderSender initialized");
        } catch (TelegramApiException e) {
            LOGGER.error("An error occurred while bot registering", e);
        }
    }

    public static void initReminderSender(ScheduleBot bot) {
        ReminderService reminderService = new ReminderService();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() ->
                reminderService.handleActualReminders(bot), 0, 30, TimeUnit.SECONDS);
    }
}
