package ru.madbunny.schedule.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.madbunny.schedule.bot.dao.UserDao;

import java.io.Serializable;

public class ScheduleBot extends TelegramLongPollingCommandBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    private final String botName;
    private final String botToken;

    private final ResponseService responseService;

    public ScheduleBot(String botName, String botToken) {
        this.botName = botName;
        this.botToken = botToken;

        responseService = new ResponseService();
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        var msg = update.getMessage();

        var preparedResponse = responseService.prepare(msg);

        if (preparedResponse instanceof BotApiMethod) {
            try {
                execute((BotApiMethod<? extends Serializable>) preparedResponse);
            } catch (TelegramApiException e) {
                LOGGER.error("An error occurred while sending message: {}", e.getMessage());
            }
            return;
        }
        throw new RuntimeException("Can't handle message");
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
