package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.message.LocalisationService;

@Component
public class GoBackCommand implements KeyboardBotCommand {

    private CommandNavigator commandNavigator;

    private String description;

    @Autowired
    public GoBackCommand(LocalisationService messageSource, CommandNavigator commandNavigator) {
        this.description = messageSource.getCurrentLocaleMessage(MessagesProperties.GO_BACK_COMMAND_NAME);
        this.commandNavigator = commandNavigator;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return description.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        commandNavigator.pop(message.getChatId());

        return false;
    }
}
