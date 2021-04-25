package ru.madbunny.schedule.bot;

import com.google.common.base.Strings;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.madbunny.schedule.bot.dao.ReminderDao;
import ru.madbunny.schedule.bot.dao.UserDao;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ResponseService {

    private static final String DATE_ATTRIBUTE = "date";

    private final UserDao userDao;
    private final ReminderDao reminderDao;

    //TODO: можно создавать команды наследниками класса
    private final static String AVAILABLE_COMMANDS = """
            /start - Начать работу с ботом
            /get_nickname - Посмотреть, под каким именем записаны в базе
            /set_nickname - Поменять имя
            /create_reminder - Создать напоминание
            /show_reminders - Посмотреть созданные напоминания
            /delete_reminder - Удалить напоминание
            """;

    private Map<Long, ConversationContext> conversationContextForChatId;

    public ResponseService() {
        //TODO: Зависимость
        userDao = new UserDao();
        reminderDao = new ReminderDao();
        conversationContextForChatId = new ConcurrentHashMap<>();
    }

    public PartialBotApiMethod<? extends Serializable> prepare(Message message) {
        var chatId = message.getChatId();
        var conversationContext = conversationContextForChatId
                .getOrDefault(chatId, new ConversationContext(ConversationState.NONE));

        // State handling
        if (ConversationState.ON_CHANGE_NICKNAME.equals(conversationContext.getState())) {
            return handleOnSetNicknameState(message);
        }
        if (ConversationState.ON_CREATE_REMINDER_SET_TIMESTAMP.equals(conversationContext.getState())) {
            return handleOnCreateReminderSetTimestamp(message);
        }
        if (ConversationState.ON_CREATE_REMINDER_SET_DESCRIPTION.equals(conversationContext.getState())) {
            return handleOnCreateReminderSetDescription(message);
        }

        // Commands handling
        var text = message.getText();
        // TODO: Перенести на "стратегию"
        if (text.startsWith("/start")) {
            return handleStartCommand(message);
        }
        if (text.startsWith("/get_nickname")) {
            return handleGetNicknameCommand(message);
        }
        if (text.startsWith("/set_nickname")) {
            return handleSetNicknameCommand(message);
        }
        if (text.startsWith("/create_reminder")) {
            return handleCreateReminderCommand(message);
        }
        if (text.startsWith("/show_reminders")) {
            return handleShowRemindersCommand(message);
        }
        if (text.startsWith("/delete_reminder")) {
            return handleDeleteReminderCommand(message);
        }
        return handlePlainText(message);
    }

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
            userDao.create(message.getFrom().getId(), name, chatId);
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

        conversationContextForChatId.put(chatId, new ConversationContext(ConversationState.ON_CHANGE_NICKNAME));

        return answer;
    }

    public PartialBotApiMethod<? extends Serializable> handleCreateReminderCommand(Message message) {
        var chatId = message.getChatId();

        var answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("""
                Введите дату и время напоминания в формате dd.MM.yyyy HH:mm:ss
                """);

        conversationContextForChatId.put(chatId, new ConversationContext(ConversationState.ON_CREATE_REMINDER_SET_TIMESTAMP));

        return answer;
    }

    public PartialBotApiMethod<? extends Serializable> handleShowRemindersCommand(Message message) {
        var chatId = message.getChatId();
        var answer = new SendMessage();
        answer.setChatId(chatId.toString());

        var user = userDao.getByTelegramId(message.getFrom().getId());
        if (null == user) {
            answer.setText("Не была пройдена регистрация. Воспользуйтесь /start.");
            return answer;
        }

        var reminders = reminderDao.getForUser(user.getId());

        if (reminders.isEmpty()) {
            answer.setText("Напоминаний нет. Чтобы создать новое воспользуйтесь /create_reminder");
            return answer;
        }

        var remindersText = reminders.stream()
                .map(rem -> "id: %d; Время напоминания: %s; Описание: %s"
                        .formatted(rem.getId(), rem.getReminder_time().toString(), rem.getDescription()))
                .collect(Collectors.joining("\n"));

        answer.setText("""
                Ваши напоминания:
                %s""".formatted(remindersText));

        return answer;
    }

    public PartialBotApiMethod<? extends Serializable> handleDeleteReminderCommand(Message message) {
        var chatId = message.getChatId();

        var answer = new SendMessage();
        answer.setChatId(chatId.toString());
        answer.setText("""
                ********
                """);

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
                userDao.create(message.getFrom().getId(), newName, chatId);
            } else {
                userDao.updateNickname(current.getId(), newName);
            }

            answer.setText("Очень приятно, %s".formatted(newName));
        }

        conversationContextForChatId.put(chatId, new ConversationContext(ConversationState.NONE));
        return answer;
    }

    public PartialBotApiMethod<? extends Serializable> handleOnCreateReminderSetTimestamp(Message message) {
        var chatId = message.getChatId();
        var answer = new SendMessage();
        answer.setChatId(chatId.toString());

        Instant date;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm:ss").withZone(ZoneOffset.UTC);
            date = formatter.parse(message.getText(), Instant::from);
        } catch (DateTimeParseException ex) {
            answer.setText("Неправильный формат даты");
            conversationContextForChatId.put(chatId, new ConversationContext(ConversationState.NONE));
            return answer;
        }

        conversationContextForChatId.put(chatId, new ConversationContext(
                ConversationState.ON_CREATE_REMINDER_SET_DESCRIPTION, Map.of(DATE_ATTRIBUTE, date)));

        answer.setText("Отлично, теперь введите описание");
        return answer;
    }

    public PartialBotApiMethod<? extends Serializable> handleOnCreateReminderSetDescription(Message message) {
        var chatId = message.getChatId();
        var answer = new SendMessage();
        answer.setChatId(chatId.toString());


        Instant date = (Instant) conversationContextForChatId.get(chatId).getAttributes().get(DATE_ATTRIBUTE);
        String text = message.getText();
        var user = userDao.getByTelegramId(message.getFrom().getId());
        if (null == date || Strings.isNullOrEmpty(text) || null == user) {
            answer.setText("Что-то пошло не так");
            conversationContextForChatId.put(chatId, new ConversationContext(ConversationState.NONE));
            return answer;
        }

        reminderDao.addReminder(date, user.getId(), text);

        answer.setText("Успех!");

        conversationContextForChatId.put(chatId, new ConversationContext(ConversationState.NONE));
        return answer;
    }


}
