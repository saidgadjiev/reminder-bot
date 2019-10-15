package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import ru.gadjini.reminder.properties.BotProperties;

@Service
public class TelegramService extends DefaultAbsSender {

    private BotProperties botProperties;

    public TelegramService(BotProperties botProperties) {
        super(ApiContext.getInstance(DefaultBotOptions.class));
        this.botProperties = botProperties;
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }
}
