package ru.madbunny.schedule.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.madbunny.schedule.bot.dao.ReminderDao;
import ru.madbunny.schedule.bot.dao.UserDao;
import ru.madbunny.schedule.bot.dto.Reminder;

import java.time.Instant;

public class ReminderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderDao reminderDao;
    private final UserDao userDao;

    public ReminderService() {
        //TODO: Зависимость
        reminderDao = new ReminderDao();
        userDao = new UserDao();
    }

    public void handleActualReminders(ScheduleBot bot) {
        LOGGER.info("run handleActualReminders");
        int successful = 0;
        var actual = reminderDao.getActual(Instant.now());

        for (Reminder rem : actual) {
            var message = new SendMessage();
            var user = userDao.get(rem.getUser_id());

            message.setChatId(String.valueOf(user.getChatId()));
            message.setText("""
                    Сработало напоминание:
                    %s
                    """.formatted(rem.getDescription()));

            try {
                bot.execute(message);
                reminderDao.markAsCompleted(rem.getId());
                successful++;
            } catch (TelegramApiException ex) {
                LOGGER.error("Oups...", ex);
            }
        }
        if (successful > 0) {
            LOGGER.info("%s reminders sent".formatted(successful));
        }
    }
}
