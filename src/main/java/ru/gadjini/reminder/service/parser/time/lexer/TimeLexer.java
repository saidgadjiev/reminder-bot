package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.parser.api.Lexem;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class TimeLexer {

    private RepeatTimeLexer repeatTimeLexer;

    private OffsetTimeLexer offsetTimeLexer;

    private FixedTimeLexer fixedTimeLexer;

    private String str;

    private int end;

    public TimeLexer(TimeLexerConfig timeLexerConfig, String str, Locale locale) {
        this(timeLexerConfig, str, false, locale);
    }

    public TimeLexer(TimeLexerConfig timeLexerConfig, String str, boolean fullMatch, Locale locale) {
        this(timeLexerConfig, str, fullMatch, false, locale);
    }

    public TimeLexer(TimeLexerConfig timeLexerConfig, String str, boolean fullMatch, boolean withoutRepeatWord, Locale locale) {
        this.str = str;

        str = str.toLowerCase();
        this.repeatTimeLexer = new RepeatTimeLexer(timeLexerConfig, str, fullMatch, withoutRepeatWord, locale);
        this.fixedTimeLexer = new FixedTimeLexer(timeLexerConfig, str, fullMatch, locale);
        this.offsetTimeLexer = new OffsetTimeLexer(timeLexerConfig, str, fullMatch, locale);
    }

    public List<Lexem> tokenizeThrowParseException() {
        List<Lexem> lexems = tokenize();

        if (lexems == null) {
            throw new ParseException();
        }

        return lexems;
    }

    public LinkedList<Lexem> tokenize() {
        LinkedList<Lexem> lexems = offsetTimeLexer.tokenize();

        if (lexems != null) {
            lexems.addFirst(new Lexem(TimeToken.OFFSET, ""));
            end = offsetTimeLexer.end();
            return lexems;
        }

        lexems = repeatTimeLexer.tokenize();

        if (lexems != null) {
            end = repeatTimeLexer.end();
            lexems.addFirst(new Lexem(TimeToken.REPEAT, ""));
            return lexems;
        }

        lexems = fixedTimeLexer.tokenize();

        if (lexems != null) {
            end = fixedTimeLexer.end();
            return lexems;
        }

        return null;
    }

    public String eraseTime() {
        return str.substring(0, str.length() - end).trim();
    }
}
