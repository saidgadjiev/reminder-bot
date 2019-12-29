package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class UserSettingsCommand implements KeyboardBotCommand, NavigableBotCommand {

    private String name;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    @Autowired
    public UserSettingsCommand(LocalisationService localisationService, MessageService messageService, ReplyKeyboardService replyKeyboardService) {
        this.name = localisationService.getMessage(MessagesProperties.USER_SETTINGS_COMMAND_NAME);
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        messageService.sendMessageByCode(
                message.getChatId(),
                MessagesProperties.MESSAGE_USER_SETTINGS,
                replyKeyboardService.getUserSettingsKeyboard()
        );
        return true;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.USER_SETTINGS_COMMAND_HISTORY_NAME;
    }

    @Override
    public String getParentHistoryName() {
        return MessagesProperties.START_COMMAND_NAME;
    }

    @Override
    public void restore(long chatId) {
        messageService.sendMessageByCode(
                chatId,
                MessagesProperties.MESSAGE_USER_SETTINGS,
                replyKeyboardService.getUserSettingsKeyboard()
        );
    }
}
