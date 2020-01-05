package ru.gadjini.reminder.bot.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class HelpCommand extends BotCommand {

    private final MessageService messageService;

    @Autowired
    public HelpCommand(MessageService messageService) {
        super(CommandNames.HELP_COMMAND_NAME, "");
        this.messageService = messageService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        messageService.sendMessageByCode(chat.getId(), MessagesProperties.MESSAGE_HELP);
    }
}
