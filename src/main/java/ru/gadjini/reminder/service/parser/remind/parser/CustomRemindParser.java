package ru.gadjini.reminder.service.parser.remind.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.parser.ParseException;
import ru.gadjini.reminder.service.parser.remind.lexer.CustomRemindLexem;
import ru.gadjini.reminder.service.parser.remind.lexer.CustomReminderToken;

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
        if (check(lexems, CustomReminderToken.TYPE)) {
            consumeType(lexems);

            return parsedCustomRemind;
        } else if (check(lexems, CustomReminderToken.TTYPE)) {
            consumeTType(lexems);

            return parsedCustomRemind;
        }

        throw new ParseException();
    }

    private void consumeType(List<CustomRemindLexem> lexems) {
        String type = consume(lexems, CustomReminderToken.TYPE).getValue();
        if (type.equals(typeAfter)) {
            parsedCustomRemind.setType(ParsedCustomRemind.Type.AFTER);
        } else if (type.equals(typeBefore)) {
            parsedCustomRemind.setType(ParsedCustomRemind.Type.BEFORE);
        } else {
            throw new ParseException();
        }

        if (check(lexems, CustomReminderToken.HOUR)) {
            int hour = Integer.parseInt(consume(lexems, CustomReminderToken.HOUR).getValue());
            parsedCustomRemind.setHour(hour);
        }
        if (check(lexems, CustomReminderToken.MINUTE)) {
            int minute = Integer.parseInt(consume(lexems, CustomReminderToken.MINUTE).getValue());
            parsedCustomRemind.setMinute(minute);
        }
    }

    private void consumeTType(List<CustomRemindLexem> lexems) {
        String type = consume(lexems, CustomReminderToken.TTYPE).getValue();

        if (!type.equals(typeAt)) {
            throw new ParseException();
        }
        parsedCustomRemind.setType(ParsedCustomRemind.Type.AT);

        if (check(lexems, CustomReminderToken.THOUR)) {
            int hour = Integer.parseInt(consume(lexems, CustomReminderToken.THOUR).getValue());
            parsedCustomRemind.setHour(hour);
        }
        if (check(lexems, CustomReminderToken.TMINUTE)) {
            int minute = Integer.parseInt(consume(lexems, CustomReminderToken.TMINUTE).getValue());
            parsedCustomRemind.setMinute(minute);
        }
    }

    private CustomRemindLexem consume(List<CustomRemindLexem> lexems, CustomReminderToken token) {
        CustomRemindLexem lexem = get(lexems);

        if (lexem != null && lexem.getToken().equals(token)) {
            ++position;

            return lexem;
        }

        throw new ParseException();
    }

    private boolean check(List<CustomRemindLexem> lexems, CustomReminderToken token) {
        CustomRemindLexem lexem = get(lexems);

        return lexem!= null && lexem.getToken().equals(token);
    }

    private CustomRemindLexem get(List<CustomRemindLexem> lexems) {
        return position >= lexems.size() ? null : lexems.get(position);
    }
}
