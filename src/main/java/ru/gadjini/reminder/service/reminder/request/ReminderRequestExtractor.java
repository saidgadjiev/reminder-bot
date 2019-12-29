package ru.gadjini.reminder.service.reminder.request;

import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

public interface ReminderRequestExtractor {

    ReminderRequestExtractor setNext(ReminderRequestExtractor next);

    ReminderRequest extract(ReminderRequestContext context);
}
