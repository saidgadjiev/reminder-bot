package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class RemindersCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private String name;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public RemindersCommand(LocalisationService localisationService, MessageService messageService, InlineKeyboardService inlineKeyboardService) {
        this.name = localisationService.getMessage(MessagesProperties.GET_REMINDERS_COMMAND_NAME);
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        messageService.sendMessageByCode(
                message.getChatId(),
                MessagesProperties.MESSAGE_LET_SEE_ON_REMINDERS,
                inlineKeyboardService.getRemindersMenu()
        );

        return false;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        messageService.editMessageByMessageCode(
                tgMessage.getChatId(),
                tgMessage.getMessageId(),
                MessagesProperties.MESSAGE_LET_SEE_ON_REMINDERS,
                inlineKeyboardService.getRemindersMenu()
        );
    }
}
