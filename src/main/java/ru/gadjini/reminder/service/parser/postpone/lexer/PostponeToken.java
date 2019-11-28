package ru.gadjini.reminder.service.parser.postpone.lexer;

import ru.gadjini.reminder.service.parser.api.Token;

public enum PostponeToken implements Token {

    TYPE,

    ON_DAY,

    ON_HOUR,

    ON_MINUTE

}
