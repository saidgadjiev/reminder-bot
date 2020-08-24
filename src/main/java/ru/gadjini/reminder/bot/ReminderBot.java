package ru.gadjini.reminder.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.filter.BotFilter;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.property.BotProperties;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class ReminderBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderBot.class);

    private BotProperties botProperties;

    private BotFilter botFilter;

    private MessageService messageService;

    private TgUserService userService;

    @Autowired
    public ReminderBot(DefaultBotOptions botOptions, BotProperties botProperties, BotFilter botFilter,
                       MessageService messageService, TgUserService userService) {
        super(botOptions);
        this.botProperties = botProperties;
        this.botFilter = botFilter;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            botFilter.doFilter(update);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);

            TgMessage tgMessage = TgMessage.from(update);
            messageService.sendErrorMessage(TgMessage.getChatId(update), userService.getLocale(tgMessage.getUser().getId()), ex);
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }
}
