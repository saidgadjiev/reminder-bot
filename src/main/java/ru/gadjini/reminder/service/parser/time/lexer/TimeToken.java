package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.service.parser.api.Token;

public enum TimeToken implements Token {

    DAY,

    MONTH,

    MONTH_WORD,

    DAY_WORD,

    DAY_OF_WEEK,

    NEXT_WEEK,

    HOUR,

    MINUTE,

    MINUTES,

    HOURS,

    EVERY_DAY,

    EVERY_MINUTE,

    EVERY_HOUR,

    DAYS,

    REPEAT,

    OFFSET

}
