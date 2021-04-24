package ru.madbunny.schedule.bot;

import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ScheduleBot extends TelegramLongPollingCommandBot {


    private final String botName;
    private final String botToken;

    public ScheduleBot(String botName, String botToken) {

        this.botName = botName;
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        var msg = update.getMessage();
        var chatId = msg.getChatId();

        var answer = new SendMessage();
        answer.setText("Hello World!");
        answer.setChatId(chatId.toString());

        try {
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
