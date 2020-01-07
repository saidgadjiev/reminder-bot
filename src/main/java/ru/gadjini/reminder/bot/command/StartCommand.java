package ru.gadjini.reminder.bot.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.request.ReminderRequestContext;

@Component
public class StartCommand extends BotCommand implements NavigableBotCommand {

    private MessageService messageService;

    private ReminderRequestService reminderRequestService;

    private ReplyKeyboardService replyKeyboardService;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public StartCommand(MessageService messageService,
                        ReminderRequestService reminderRequestService,
                        ReplyKeyboardService replyKeyboardService,
                        ReminderMessageSender reminderMessageSender) {
        super(CommandNames.START_COMMAND_NAME, "");
        this.messageService = messageService;
        this.reminderRequestService = reminderRequestService;
        this.replyKeyboardService = replyKeyboardService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] args) {
        messageService.sendMessageByCode(chat.getId(), MessagesProperties.MESSAGE_START, replyKeyboardService.getMainMenu());
    }

    @Override
    public String getHistoryName() {
        return CommandNames.START_COMMAND_NAME;
    }

    @Override
    public ReplyKeyboardMarkup silentRestore() {
        return replyKeyboardService.getMainMenu();
    }

    @Override
    public boolean accept(Message message) {
        return message.hasText() || message.hasVoice();
    }

    @Override
    public void restore(long chatId) {
        messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_START, replyKeyboardService.getMainMenu());
    }

    @Override
    public String getParentHistoryName() {
        return null;
    }

    @Override
    public void processNonCommandUpdate(Message message, String reminderText) {
        Reminder reminder = reminderRequestService.createReminder(
                new ReminderRequestContext()
                        .setText(reminderText)
                        .setVoice(message.hasVoice())
                        .setUser(message.getFrom())
                        .setMessageId(message.getMessageId()));
        reminder.getCreator().setChatId(message.getChatId());

        reminderMessageSender.sendReminderCreated(reminder, null);
    }

    @Override
    public void processNonCommandEditedMessage(Message editedMessage, String text) {
        UpdateReminderResult updateReminderResult = reminderRequestService.updateReminder(editedMessage.getMessageId(), text);
        if (updateReminderResult == null) {
            return;
        }
        updateReminderResult.getOldReminder().getCreator().setChatId(editedMessage.getChatId());

        reminderMessageSender.sendReminderFullyUpdate(updateReminderResult);
    }
}
