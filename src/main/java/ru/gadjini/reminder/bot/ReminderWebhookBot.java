package ru.gadjini.reminder.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.properties.BotProperties;
import ru.gadjini.reminder.service.filter.BotFilter;

@Component
public class ReminderWebhookBot extends TelegramWebhookBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderWebhookBot.class);

    private BotProperties botProperties;

    private BotFilter botFilter;

    @Autowired
    public ReminderWebhookBot(BotProperties botProperties, BotFilter botFilter) {
        this.botProperties = botProperties;
        this.botFilter = botFilter;
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        try {
            botFilter.doFilter(update);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return null;
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public String getBotPath() {
        return botProperties.getName();
    }
}
