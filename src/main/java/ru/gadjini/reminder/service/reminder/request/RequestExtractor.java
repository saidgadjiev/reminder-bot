package ru.gadjini.reminder.service.reminder.request;

import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

public interface RequestExtractor {

    RequestExtractor setNext(RequestExtractor next);

    ReminderRequest extract(String text, Integer receiverId, boolean voice);
}
