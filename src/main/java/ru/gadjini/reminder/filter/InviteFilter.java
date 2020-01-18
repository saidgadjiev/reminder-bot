package ru.gadjini.reminder.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.service.InviteService;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CommandParser;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardServiceImpl;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class InviteFilter extends BaseBotFilter {

    private CommandParser commandParser;

    private TgUserService userService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private InviteService inviteService;

    private StartCommandFilter startCommandFilter;

    private ReplyKeyboardService replyKeyboardService;

    @Autowired
    public InviteFilter(CommandParser commandParser, TgUserService userService, MessageService messageService,
                        LocalisationService localisationService, InviteService inviteService,
                        StartCommandFilter startCommandFilter, ReplyKeyboardServiceImpl replyKeyboardService) {
        this.commandParser = commandParser;
        this.userService = userService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.inviteService = inviteService;
        this.startCommandFilter = startCommandFilter;
        this.replyKeyboardService = replyKeyboardService;
    }

    @Override
    public void doFilter(Update update) {
        TgMessage message = TgMessage.from(update);
        boolean exists = userService.isExists(message.getUser().getId());

        if (!exists) {
            if (isStartCommand(update)) {
                messageService.sendMessageAsync(
                        new SendMessageContext(PriorityJob.Priority.MEDIUM)
                                .chatId(message.getChatId())
                                .text(localisationService.getMessage(MessagesProperties.MESSAGE_BOT_CLOSE_TESTING))
                                .replyKeyboard(replyKeyboardService.removeKeyboard(message.getChatId()))
                );
            } else {
                String token = inviteService.delete(message.getText());

                if (token != null) {
                    startCommandFilter.doStart(update);

                    super.doFilter(update);
                } else {
                    messageService.sendMessageAsync(
                            new SendMessageContext(PriorityJob.Priority.MEDIUM)
                                    .chatId(message.getChatId())
                                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_INVITE_TOKEN_NOT_FOUND))
                                    .replyKeyboard(replyKeyboardService.removeKeyboard(message.getChatId()))
                    );
                }
            }
        } else {
            super.doFilter(update);
        }
    }

    private boolean isStartCommand(Update update) {
        if (update.hasMessage()) {
            String commandName = commandParser.parseBotCommandName(update.getMessage());

            return commandName.equals(CommandNames.START_COMMAND_NAME);
        }

        return false;
    }
}
