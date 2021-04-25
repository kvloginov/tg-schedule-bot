package ru.madbunny.schedule.bot;

import com.google.common.base.Strings;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.madbunny.schedule.bot.dao.UserDao;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseService {

    private final UserDao userDao;

    private final static String AVAILABLE_COMMANDS = """
            /get_nickname - посмотреть, под каким именем записаны в базе
            /set_nickname - поменять имя
            """;

    private Map<Long, ConversationState> conversationStateForChatId;

    public ResponseService() {
        //TODO: Зависимость
        userDao = new UserDao();
        conversationStateForChatId = new ConcurrentHashMap<>();
    }

    public PartialBotApiMethod<? extends Serializable> prepare(Message message) {
        var chatId = message.getChatId();
        var conversationState = conversationStateForChatId.getOrDefault(chatId, ConversationState.NONE);

        if (ConversationState.ON_CHANGE_NICKNAME.equals(conversationState)) {
            return handleOnSetNicknameState(message);
        }

        var text = message.getText();
        if (text.startsWith("/start")) {
            return handleStartCommand(message);
        }
        if (text.startsWith("/get_nickname")) {
            return handleGetNicknameCommand(message);
        }
        if (text.startsWith("/set_nickname")) {
            return handleSetNicknameCommand(message);
        }
        return handlePlainText(message);
    }

    //TODO: Перенести на "стратегию"
    public PartialBotApiMethod<? extends Serializable> handleStartCommand(Message message) {
        var chatId = message.getChatId();
        var answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("""
                Добро пожаловать!
                Доступные команды:
                %s
                """.formatted(AVAILABLE_COMMANDS));

        if (null == userDao.getByTelegramId(message.getFrom().getId())) {
            var name = message.getFrom().getFirstName() + " "
                    + message.getFrom().getLastName() + " ("
                    + message.getFrom().getUserName() + ")";
            userDao.create(message.getFrom().getId(), name);
        }

        return answer;
    }

    public PartialBotApiMethod<? extends Serializable> handlePlainText(Message message) {
        var chatId = message.getChatId();
        var answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("""
                К сожалению, команда не распознана. Можете воспользоваться доступными командами:
                %s
                """.formatted(AVAILABLE_COMMANDS));

        return answer;
    }

    public PartialBotApiMethod<? extends Serializable> handleGetNicknameCommand(Message message) {
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

    public PartialBotApiMethod<? extends Serializable> handleSetNicknameCommand(Message message) {
        var chatId = message.getChatId();

        var answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("""
                Отлично! Введите свое новое имя
                """);

        conversationStateForChatId.put(chatId, ConversationState.ON_CHANGE_NICKNAME);

        return answer;
    }

    public PartialBotApiMethod<? extends Serializable> handleOnSetNicknameState(Message message) {
        var chatId = message.getChatId();
        var answer = new SendMessage();
        answer.setChatId(chatId.toString());

        var newName = message.getText();
        if (Strings.isNullOrEmpty(newName) || newName.length() < 2) {
            answer.setText("Кажется, имя задано неправильно :C");
        } else {
            var current = userDao.getByTelegramId(message.getFrom().getId());
            if (null == current) {
                userDao.create(message.getFrom().getId(), newName);
            } else {
                userDao.updateNickname(current.getId(), newName);
            }

            answer.setText("Очень приятно, %s".formatted(newName));
        }

        conversationStateForChatId.put(chatId, ConversationState.NONE);
        return answer;
    }
}
