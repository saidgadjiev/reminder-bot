package ru.gadjini.reminder.service.filter;

import org.telegram.telegrambots.meta.api.objects.Update;

public class BaseBotFilter implements BotFilter {

    private BotFilter next;

    @Override
    public BotFilter setNext(BotFilter next) {
        this.next = next;

        return next;
    }

    @Override
    public void doFilter(Update update) {
        if (next != null) {
            next.doFilter(update);
        }
    }
}
