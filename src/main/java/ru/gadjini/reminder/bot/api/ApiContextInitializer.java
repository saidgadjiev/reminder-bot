package ru.gadjini.reminder.bot.api;

import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.Webhook;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class ApiContextInitializer {
    private ApiContextInitializer() {
    }

    public static void init() {
        ApiContext.register(BotSession.class, DefaultBotSession.class);
        ApiContext.registerSingleton(Webhook.class, DefaultWebhook.class);
    }
}
