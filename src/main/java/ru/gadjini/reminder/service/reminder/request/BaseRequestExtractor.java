package ru.gadjini.reminder.service.reminder.request;

import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

public class BaseRequestExtractor implements RequestExtractor {

    private RequestExtractor next;

    @Override
    public RequestExtractor setNext(RequestExtractor next) {
        this.next = next;

        return next;
    }

    @Override
    public ReminderRequest extract(String text, Integer receiverId) {
        if (next != null) {
            next.extract(text, receiverId);
        }

        return null;
    }
}
