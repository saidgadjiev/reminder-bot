package ru.gadjini.reminder.service.reminder.request;

import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

public class BaseRequestExtractor implements ReminderRequestExtractor {

    private ReminderRequestExtractor next;

    @Override
    public ReminderRequestExtractor setNext(ReminderRequestExtractor next) {
        this.next = next;

        return next;
    }

    @Override
    public ReminderRequest extract(ReminderRequestContext context) {
        if (next != null) {
            return next.extract(context);
        }

        return null;
    }
}
