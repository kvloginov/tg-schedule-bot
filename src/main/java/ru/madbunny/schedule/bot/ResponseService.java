package ru.madbunny.schedule.bot;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.madbunny.schedule.bot.dao.UserDao;

import java.io.Serializable;

public class ResponseService {

    private final UserDao userDao;

    public ResponseService() {
        //TODO: Зависимость
        userDao = new UserDao();
    }

    public PartialBotApiMethod<? extends Serializable> prepare(Message message) {
        var chatId = message.getChatId();


        var text = message.getText();
        if (text.startsWith("/start")) {
            return handleStart(message);
        }
        if (text.startsWith("/getNickname")) {
            return handleGetNickname(message);
        }
        if (text.startsWith("/setNickname")) {
            return handleSetNickname(message);
        }
        return null;
    }

    //TODO: Перенести на "стратегию"
    public PartialBotApiMethod<? extends Serializable> handleStart(Message message) {
        var chatId = message.getChatId();
        var answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("""
                Добро пожаловать!
                Доступные команды:
                /getNickname - посмотреть, под каким именем записаны в базе
                /setNickname - поменять имя
                """);

        if (null == userDao.getByTelegramId(message.getFrom().getId())) {
            var name = message.getFrom().getFirstName() + " "
                    + message.getFrom().getLastName() + " ("
                    + message.getFrom().getUserName() + ")";
            userDao.create(message.getFrom().getId(), name);
        }

        return answer;
    }

    public PartialBotApiMethod<? extends Serializable> handleGetNickname(Message message) {
        var chatId = message.getChatId();
        var answer = new SendMessage();
        answer.setChatId(chatId.toString());

        var user = userDao.getByTelegramId(message.getFrom().getId());
        if (null == user) {
            answer.setText("""
                    К сожалению, мы вас не знаем.
                    Чтобы зарегистрироваться наберите /start
                    """);
        } else {
            answer.setText("Ваше имя: %s".formatted(user.getNickName()));
        }


        return answer;
    }

    public PartialBotApiMethod<? extends Serializable> handleSetNickname(Message message) {
        var chatId = message.getChatId();
        var answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("""
                Пока такого не умеем(
                """);

        return answer;
    }
}
