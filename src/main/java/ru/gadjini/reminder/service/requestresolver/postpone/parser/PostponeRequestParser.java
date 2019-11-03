package ru.gadjini.reminder.service.requestresolver.postpone.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.requestresolver.postpone.lexer.PostponeLexem;
import ru.gadjini.reminder.service.requestresolver.postpone.lexer.PostponeToken;
import ru.gadjini.reminder.service.requestresolver.reminder.parser.ParseException;

import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class PostponeRequestParser {

    private ParsedPostponeTime postponeTime = new ParsedPostponeTime();

    private int position;

    private String typeOn;

    private String typeAt;

    private String tomorrow;

    private String dayAfterTomorrow;

    private final Locale locale;

    public PostponeRequestParser(LocalisationService localisationService, Locale locale) {
        typeOn = localisationService.getMessage(MessagesProperties.REGEX_POSTPONE_TYPE_ON);
        typeAt = localisationService.getMessage(MessagesProperties.REGEX_POSTPONE_TYPE_AT);
        tomorrow = localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TOMORROW);
        dayAfterTomorrow = localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_DAY_AFTER_TOMORROW);
        this.locale = locale;
    }

    public ParsedPostponeTime parse(List<PostponeLexem> lexems) {
        String type = consume(lexems, PostponeToken.TYPE).getValue();
        if (type.equals(typeOn)) {
            postponeTime.setPostponeOn(new PostponeOn());
            consumeTypeOn(lexems);
        } else if (type.equals(typeAt)) {
            postponeTime.setPostponeAt(new PostponeAt());
            consumeTypeAt(lexems);
        } else {
            throw new ParseException();
        }

        if (position < lexems.size()) {
            throw new ParseException();
        }

        return postponeTime;
    }

    private void consumeTypeOn(List<PostponeLexem> lexems) {
        if (check(lexems, PostponeToken.ONDAY)) {
            int day = Integer.parseInt(consume(lexems, PostponeToken.ONDAY).getValue());

            postponeTime.getPostponeOn().setDay(day);
        }
        if (check(lexems, PostponeToken.ONHOUR)) {
            int hour = Integer.parseInt(consume(lexems, PostponeToken.ONHOUR).getValue());

            postponeTime.getPostponeOn().setHour(hour);
        }
        if (check(lexems, PostponeToken.ONMINUTE)) {
            int minute = Integer.parseInt(consume(lexems, PostponeToken.ONMINUTE).getValue());

            postponeTime.getPostponeOn().setMinute(minute);
        }
    }

    private void consumeTypeAt(List<PostponeLexem> lexems) {
        if (check(lexems, PostponeToken.MONTH)) {
            consumeMonth(lexems);
        } else if (check(lexems, PostponeToken.DAYWORD)) {
            consumeDayWord(lexems);
        } else if (check(lexems, PostponeToken.DAY)) {
            consumeDay(lexems);
        } else if (check(lexems, PostponeToken.HOUR)) {
            postponeTime.getPostponeAt().setTime(consumeTime(lexems));
        }
    }

    private void consumeMonth(List<PostponeLexem> lexems) {
        int month = Integer.parseInt(consume(lexems, PostponeToken.MONTH).getValue());
        int day = Integer.parseInt(consume(lexems, PostponeToken.DAY).getValue());

        postponeTime.getPostponeAt().setMonth(month);
        postponeTime.getPostponeAt().setDay(day);
        postponeTime.getPostponeAt().setTime(consumeTime(lexems));
    }

    private void consumeMonthWord(List<PostponeLexem> lexems) {
        String month = consume(lexems, PostponeToken.MONTHWORD).getValue();

        Month m = Stream.of(Month.values()).filter(item -> item.getDisplayName(TextStyle.FULL, locale).equals(month)).findFirst().orElseThrow(ParseException::new);

        postponeTime.getPostponeAt().setMonth(m.getValue());
        postponeTime.getPostponeAt().setTime(consumeTime(lexems));
    }

    private void consumeDayWord(List<PostponeLexem> lexems) {
        String dayWord = consume(lexems, PostponeToken.DAYWORD).getValue();

        if (dayWord.equals(tomorrow)) {
            postponeTime.getPostponeAt().setAddDays(1);
            postponeTime.getPostponeAt().setTime(consumeTime(lexems));
        } else if (dayWord.equals(dayAfterTomorrow)) {
            postponeTime.getPostponeAt().setAddDays(2);
            postponeTime.getPostponeAt().setTime(consumeTime(lexems));
        }
    }

    private void consumeDay(List<PostponeLexem> lexems) {
        int day = Integer.parseInt(consume(lexems, PostponeToken.DAY).getValue());

        postponeTime.getPostponeAt().setDay(day);
        if (check(lexems, PostponeToken.MONTHWORD)) {
            consumeMonthWord(lexems);
        } else {
            postponeTime.getPostponeAt().setTime(consumeTime(lexems));
        }
    }

    private LocalTime consumeTime(List<PostponeLexem> lexems) {
        int hour = Integer.parseInt(consume(lexems, PostponeToken.HOUR).getValue());
        int minute = Integer.parseInt(consume(lexems, PostponeToken.MINUTE).getValue());

        return LocalTime.of(hour, minute);
    }

    private PostponeLexem consume(List<PostponeLexem> lexems, PostponeToken token) {
        PostponeLexem lexem = get(lexems);

        if (lexem != null && lexem.getToken().equals(token)) {
            ++position;

            return lexem;
        }

        throw new ParseException();
    }

    private boolean check(List<PostponeLexem> lexems, PostponeToken token) {
        PostponeLexem lexem = get(lexems);

        return lexem!= null && lexem.getToken().equals(token);
    }

    private PostponeLexem get(List<PostponeLexem> lexems) {
        return position >= lexems.size() ? null : lexems.get(position);
    }
}
