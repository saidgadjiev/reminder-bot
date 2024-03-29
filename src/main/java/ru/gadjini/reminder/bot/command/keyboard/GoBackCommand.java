package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class GoBackCommand implements KeyboardBotCommand {

    private CommandNavigator commandNavigator;

    private Set<String> names = new HashSet<>();

    @Autowired
    public GoBackCommand(LocalisationService localisationService) {
        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale));
        }
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        commandNavigator.pop(TgMessage.from(message));

        return false;
    }
}
