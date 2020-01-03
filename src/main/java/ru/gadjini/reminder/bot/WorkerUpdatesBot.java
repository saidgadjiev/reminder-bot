package ru.gadjini.reminder.bot;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class WorkerUpdatesBot extends TelegramWebhookBot {

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {


        return null;
    }

    protected void onUpdateReceived(Update update) {
    }
}
