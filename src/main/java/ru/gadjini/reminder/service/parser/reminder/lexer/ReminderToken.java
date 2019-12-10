package ru.gadjini.reminder.service.parser.reminder.lexer;

import ru.gadjini.reminder.service.parser.api.Token;

public enum ReminderToken implements Token {

    LOGIN,

    TEXT,

    NOTE,

    MINUTES,

    HOURS,

    DAY_OF_WEEK,

    EVERY_DAY,

    EVERY_MINUTE,

    EVERY_HOUR,

    DAYS,

    HOUR,

    MINUTE,

    REPEAT
}
