package ru.gadjini.reminder.service.parser.remind.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.exception.UserMessageParseException;
import ru.gadjini.reminder.service.parser.remind.lexer.CustomRemindLexem;
import ru.gadjini.reminder.service.parser.remind.lexer.CustomRemindToken;

import java.util.List;

public class CustomRemindParser {

    private ParsedCustomRemind parsedCustomRemind = new ParsedCustomRemind();

    private String typeBefore;

    private String typeAfter;

    private String typeAt;

    private int position;

    public CustomRemindParser(LocalisationService localisationService) {
        this.typeBefore = localisationService.getMessage(MessagesProperties.REGEX_CUSTOM_REMIND_TYPE_BEFORE);
        this.typeAfter = localisationService.getMessage(MessagesProperties.REGEX_CUSTOM_REMIND_TYPE_AFTER);
        this.typeAt = localisationService.getMessage(MessagesProperties.REGEX_CUSTOM_REMIND_TYPE_AT);
    }

    public ParsedCustomRemind parse(List<CustomRemindLexem> lexems) {
        if (check(lexems, CustomRemindToken.TYPE)) {
            consumeType(lexems);

            return parsedCustomRemind;
        } else if (check(lexems, CustomRemindToken.TTYPE)) {
            consumeTType(lexems);

            return parsedCustomRemind;
        }

        throw new UserMessageParseException();
    }

    private void consumeType(List<CustomRemindLexem> lexems) {
        String type = consume(lexems, CustomRemindToken.TYPE).getValue();
        if (type.equals(typeAfter)) {
            parsedCustomRemind.setType(ParsedCustomRemind.Type.AFTER);
        } else if (type.equals(typeBefore)) {
            parsedCustomRemind.setType(ParsedCustomRemind.Type.BEFORE);
        } else {
            throw new UserMessageParseException();
        }

        if (check(lexems, CustomRemindToken.HOUR)) {
            int hour = Integer.parseInt(consume(lexems, CustomRemindToken.HOUR).getValue());
            parsedCustomRemind.setHour(hour);
        }
        if (check(lexems, CustomRemindToken.MINUTE)) {
            int minute = Integer.parseInt(consume(lexems, CustomRemindToken.MINUTE).getValue());
            parsedCustomRemind.setMinute(minute);
        }
    }

    private void consumeTType(List<CustomRemindLexem> lexems) {
        String type = consume(lexems, CustomRemindToken.TTYPE).getValue();

        if (!type.equals(typeAt)) {
            throw new UserMessageParseException();
        }
        parsedCustomRemind.setType(ParsedCustomRemind.Type.AT);

        if (check(lexems, CustomRemindToken.THOUR)) {
            int hour = Integer.parseInt(consume(lexems, CustomRemindToken.THOUR).getValue());
            parsedCustomRemind.setHour(hour);
        }
        if (check(lexems, CustomRemindToken.TMINUTE)) {
            int minute = Integer.parseInt(consume(lexems, CustomRemindToken.TMINUTE).getValue());
            parsedCustomRemind.setMinute(minute);
        }
    }

    private CustomRemindLexem consume(List<CustomRemindLexem> lexems, CustomRemindToken token) {
        CustomRemindLexem lexem = get(lexems);

        if (lexem != null && lexem.getToken().equals(token)) {
            ++position;

            return lexem;
        }

        throw new UserMessageParseException();
    }

    private boolean check(List<CustomRemindLexem> lexems, CustomRemindToken token) {
        CustomRemindLexem lexem = get(lexems);

        return lexem!= null && lexem.getToken().equals(token);
    }

    private CustomRemindLexem get(List<CustomRemindLexem> lexems) {
        return position >= lexems.size() ? null : lexems.get(position);
    }
}
