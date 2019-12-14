package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.message.MessageService;

public class OkCommand implements CallbackBotCommand {

    private MessageService messageService;

    public OkCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return MessagesProperties.OK_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }
}
