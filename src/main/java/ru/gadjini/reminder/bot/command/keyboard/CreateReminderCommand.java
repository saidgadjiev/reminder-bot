package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.message.LocalisationService;

@Component
public class CreateReminderCommand implements KeyboardBotCommand {

    private String name;

    @Autowired
    public CreateReminderCommand(LocalisationService localisationService) {
        this.name = localisationService.getMessage(MessagesProperties.CREATE_REMINDER_COMMAND_DESCRIPTION);
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        return false;
    }
}
