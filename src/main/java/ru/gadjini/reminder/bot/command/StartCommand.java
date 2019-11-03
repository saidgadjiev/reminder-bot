package ru.gadjini.reminder.bot.command;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.requestresolver.RequestParser;
import ru.gadjini.reminder.service.requestresolver.reminder.parser.ParseException;
import ru.gadjini.reminder.service.requestresolver.reminder.parser.ParsedRequest;
import ru.gadjini.reminder.service.validation.ErrorBag;
import ru.gadjini.reminder.service.validation.ValidationService;
import ru.gadjini.reminder.util.ReminderUtils;

import java.time.ZoneId;

public class StartCommand extends BotCommand implements NavigableBotCommand {

    private final MessageService messageService;

    private final ReminderService reminderService;

    private TgUserService tgUserService;

    private SecurityService securityService;

    private RequestParser requestParser;

    private KeyboardService keyboardService;

    private ValidationService validationService;

    private ReminderMessageSender reminderMessageSender;

    public StartCommand(MessageService messageService,
                        ReminderService reminderService,
                        TgUserService tgUserService,
                        SecurityService securityService, RequestParser requestParser,
                        KeyboardService keyboardService,
                        ValidationService validationService,
                        ReminderMessageSender reminderMessageSender) {
        super(MessagesProperties.START_COMMAND_NAME, "");
        this.messageService = messageService;
        this.reminderService = reminderService;
        this.tgUserService = tgUserService;
        this.securityService = securityService;
        this.requestParser = requestParser;
        this.keyboardService = keyboardService;
        this.validationService = validationService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] args) {
        tgUserService.createOrUpdateUser(chat.getId(), user);
        messageService.sendMessageByCode(chat.getId(), MessagesProperties.MESSAGE_START, keyboardService.getMainMenu());
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.START_COMMAND_NAME;
    }

    @Override
    public ReplyKeyboardMarkup silentRestore() {
        return keyboardService.getMainMenu();
    }

    @Override
    public void restore(long chatId) {
        messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_START, keyboardService.getMainMenu());
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        if (!message.hasText()) {
            return;
        }
        ReminderRequest reminderRequest;

        try {
            ParsedRequest parsedRequest = requestParser.parseRequest(message.getText().trim());

            reminderRequest = createReminderRequest(parsedRequest);
        } catch (ParseException ex) {
            messageService.sendMessageByCode(message.getChatId(), MessagesProperties.MESSAGE_REMINDER_FORMAT);
            return;
        }
        Reminder reminder = reminderService.createReminder(reminderRequest);
        reminder.getCreator().setChatId(message.getChatId());

        reminderMessageSender.sendReminderCreated(reminder, null);
    }

    private ReminderRequest createReminderRequest(ParsedRequest parsedRequest) {
        ReminderRequest reminderRequest = new ReminderRequest();
        ZoneId zoneId;

        if (StringUtils.isBlank(parsedRequest.getReceiverName())) {
            reminderRequest.setForMe(true);

            User currentUser = securityService.getAuthenticatedUser();
            zoneId = tgUserService.getTimeZone(currentUser.getId());
        } else {
            reminderRequest.setReceiverName(parsedRequest.getReceiverName());

            zoneId = tgUserService.getTimeZone(parsedRequest.getReceiverName());
        }
        reminderRequest.setText(parsedRequest.getText());
        reminderRequest.setRemindAt(ReminderUtils.buildRemindAt(parsedRequest.getParsedTime(), zoneId));

        return reminderRequest;
    }
}
