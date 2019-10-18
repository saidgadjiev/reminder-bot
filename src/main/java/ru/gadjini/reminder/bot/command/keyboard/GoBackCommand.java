package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.CommandNavigator;
import ru.gadjini.reminder.service.LocalisationService;

public class GoBackCommand implements KeyboardBotCommand {

    private CommandNavigator commandNavigator;

    private String description;

    public GoBackCommand(LocalisationService messageSource, CommandNavigator commandNavigator) {
        this.description = messageSource.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME);
        this.commandNavigator = commandNavigator;
    }

    @Override
    public boolean canHandle(String command) {
        return description.equals(command);
    }

    @Override
    public void processMessage(AbsSender absSender, Message message) {
        commandNavigator.pop(message.getChatId());
    }
}
