package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class FriendRequestsCommand implements KeyboardBotCommand, NavigableBotCommand {

    private ReplyKeyboardService replyKeyboardService;

    private MessageService messageService;

    private final LocalisationService localisationService;

    private TgUserService userService;

    private Set<String> names = new HashSet<>();

    @Autowired
    public FriendRequestsCommand(CurrReplyKeyboard replyKeyboardService, MessageService messageService, LocalisationService localisationService, TgUserService userService) {
        this.replyKeyboardService = replyKeyboardService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.FRIEND_REQUESTS_COMMAND_NAME, locale));
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_FRIEND_REQUESTS, userService.getLocale(message.getFrom().getId())))
                        .replyKeyboard(replyKeyboardService.getFriendRequestsKeyboard(message.getChatId()))
        );

        return true;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.FRIEND_REQUESTS_COMMAND_HISTORY_NAME;
    }
}
