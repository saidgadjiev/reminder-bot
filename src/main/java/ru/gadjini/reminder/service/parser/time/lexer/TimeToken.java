package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.service.parser.api.Token;

public enum TimeToken implements Token {

    DAY,

    MONTH,

    YEAR,

    YEARS,

    MONTH_WORD,

    DAY_WORD,

    DAY_OF_WEEK,

    NEXT_WEEK,

    HOUR,

    MINUTE,

    MINUTES,

    HOURS,

    MONTHS,

    DAYS,

    REPEAT,

    OFFSET,

    TYPE
}
