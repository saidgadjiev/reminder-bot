package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class UserSettingsCommand implements KeyboardBotCommand, NavigableBotCommand {

    private String name;

    private final LocalisationService localisationService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    @Autowired
    public UserSettingsCommand(LocalisationService localisationService, MessageService messageService, CurrReplyKeyboard replyKeyboardService) {
        this.name = localisationService.getMessage(MessagesProperties.USER_SETTINGS_COMMAND_NAME);
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_USER_SETTINGS))
                        .replyKeyboard(replyKeyboardService.getUserSettingsKeyboard(message.getChatId()))
        );
        return true;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.USER_SETTINGS_COMMAND_HISTORY_NAME;
    }

    @Override
    public String getParentHistoryName() {
        return CommandNames.START_COMMAND_NAME;
    }

    @Override
    public void restore(long chatId) {
        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(chatId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_USER_SETTINGS))
                        .replyKeyboard(replyKeyboardService.getUserSettingsKeyboard(chatId))
        );
    }

    @Override
    public ReplyKeyboardMarkup getKeyboard(long chatId) {
        return replyKeyboardService.getUserSettingsKeyboard(chatId);
    }
}
