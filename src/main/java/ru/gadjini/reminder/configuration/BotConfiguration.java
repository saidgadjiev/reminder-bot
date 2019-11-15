package ru.gadjini.reminder.configuration;

import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Configuration
public class BotConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TelegramBotsApi telegramBotsApi() throws TelegramApiRequestException {
        return new TelegramBotsApi("https://1aae0607.ngrok.io", "http://localhost:8080");
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TimeZoneEngine timeZoneEngine() {
        return TimeZoneEngine.initialize();
    }
}
