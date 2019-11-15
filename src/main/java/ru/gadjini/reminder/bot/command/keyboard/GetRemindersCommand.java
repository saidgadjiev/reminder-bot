package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.MessageService;

public class GetRemindersCommand implements KeyboardBotCommand, NavigableCallbackBotCommand {

    private String name;

    private MessageService messageService;

    private KeyboardService keyboardService;

    public GetRemindersCommand(LocalisationService localisationService, MessageService messageService, KeyboardService keyboardService) {
        this.name = localisationService.getMessage(MessagesProperties.GET_REMINDERS_COMMAND_NAME);
        this.messageService = messageService;
        this.keyboardService = keyboardService;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public void processMessage(Message message) {
        messageService.sendMessageByCode(
                message.getChatId(),
                MessagesProperties.MESSAGE_LET_SEE_ON_REMINDERS,
                keyboardService.getRemindersMenu()
        );
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME;
    }

    @Override
    public void restore(long chatId, int messageId, String queryId) {
        messageService.editMessageByMessageCode(
                chatId,
                messageId,
                MessagesProperties.MESSAGE_LET_SEE_ON_REMINDERS,
                keyboardService.getRemindersMenu()
        );
    }
}
