package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class FriendRequestsCommand implements KeyboardBotCommand, NavigableBotCommand {

    private ReplyKeyboardService replyKeyboardService;

    private MessageService messageService;

    private final LocalisationService localisationService;

    private String name;

    @Autowired
    public FriendRequestsCommand(ReplyKeyboardService replyKeyboardService, MessageService messageService, LocalisationService localisationService) {
        this.replyKeyboardService = replyKeyboardService;
        this.messageService = messageService;
        this.name = localisationService.getMessage(MessagesProperties.FRIEND_REQUESTS_COMMAND_NAME);
        this.localisationService = localisationService;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_FRIEND_REQUESTS))
                        .replyKeyboard(replyKeyboardService.getFriendRequestsKeyboard())
        );

        return true;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.FRIEND_REQUESTS_COMMAND_HISTORY_NAME;
    }
}
