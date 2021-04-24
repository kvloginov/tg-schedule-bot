package ru.madbunny.schedule.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.madbunny.schedule.bot.dao.ReminderDao;
import ru.madbunny.schedule.bot.dao.UserDao;

import java.util.Map;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String BOT_NAME_VAR_NAME = "SCHEDULE_BOT_NAME";
    private static final String BOT_TOKEN_VAR_NAME = "SCHEDULE_BOT_TOKEN";

    private static final Map<String, String> ENV = System.getenv();

    public static void main(String[] args) {
        connectToDb();

//        try {
//            var botsApi = new TelegramBotsApi(DefaultBotSession.class);
//            botsApi.registerBot(new ScheduleBot(ENV.get(BOT_NAME_VAR_NAME), ENV.get(BOT_TOKEN_VAR_NAME)));
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
    }

    public static void connectToDb() {
        ReminderDao reminderDao = new ReminderDao();
        UserDao userDao = new UserDao();

        var id = userDao.create(99000, "Kostyan!");
        userDao.get(id);
        userDao.updateNickname(id, "Now im your FATHER");
    }
}
