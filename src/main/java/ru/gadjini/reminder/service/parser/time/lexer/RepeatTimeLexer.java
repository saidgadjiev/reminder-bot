package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RepeatTimeLexer {

    private TimeLexerConfig lexerConfig;

    private String str;

    private boolean fullMatch;

    private final boolean withoutRepeatWord;

    private final Locale locale;

    private int matchEnd;

    public RepeatTimeLexer(TimeLexerConfig lexerConfig, String str, boolean fullMatch, boolean withoutRepeatWord, Locale locale) {
        this.lexerConfig = lexerConfig;
        this.str = str;
        this.fullMatch = fullMatch;
        this.withoutRepeatWord = withoutRepeatWord;
        this.locale = locale;
    }

    public LinkedList<Lexem> tokenize() {
        LinkedList<Lexem> lexems = new LinkedList<>();

        List<Map<String, String>> valuesList = getValues();

        if (valuesList != null) {
            for (Map<String, String> values : valuesList) {
                if (values.containsKey(PatternBuilder.SUFFIX_YEARS)) {
                    lexems.add(new Lexem(TimeToken.YEARS, values.get(PatternBuilder.SUFFIX_YEARS)));
                } else if (values.containsKey(PatternBuilder.PREFIX_YEARS)) {
                    lexems.add(new Lexem(TimeToken.YEARS, values.get(PatternBuilder.PREFIX_YEARS)));
                } else if (values.containsKey(PatternBuilder.ONE_YEAR)) {
                    lexems.add(new Lexem(TimeToken.YEARS, "1"));
                }

                if (values.containsKey(PatternBuilder.SUFFIX_MONTHS)) {
                    lexems.add(new Lexem(TimeToken.MONTHS, values.get(PatternBuilder.SUFFIX_MONTHS)));
                } else if (values.containsKey(PatternBuilder.PREFIX_MONTHS)) {
                    lexems.add(new Lexem(TimeToken.MONTHS, values.get(PatternBuilder.PREFIX_MONTHS)));
                } else if (values.containsKey(PatternBuilder.ONE_MONTH)) {
                    lexems.add(new Lexem(TimeToken.MONTHS, "1"));
                }

                if (values.containsKey(PatternBuilder.PREFIX_DAY_OF_MONTH)) {
                    lexems.add(new Lexem(TimeToken.DAY, values.get(PatternBuilder.PREFIX_DAY_OF_MONTH)));
                } else if (values.containsKey(PatternBuilder.SUFFIX_DAY_OF_MONTH)) {
                    lexems.add(new Lexem(TimeToken.DAY, values.get(PatternBuilder.SUFFIX_DAY_OF_MONTH)));
                }

                if (values.containsKey(PatternBuilder.SUFFIX_WEEKS)) {
                    lexems.add(new Lexem(TimeToken.WEEKS, values.get(PatternBuilder.SUFFIX_WEEKS)));
                } else if (values.containsKey(PatternBuilder.PREFIX_WEEKS)) {
                    lexems.add(new Lexem(TimeToken.WEEKS, values.get(PatternBuilder.PREFIX_WEEKS)));
                } else if (values.containsKey(PatternBuilder.ONE_WEEK)) {
                    lexems.add(new Lexem(TimeToken.WEEKS, "1"));
                }

                if (values.containsKey(PatternBuilder.WEEKS_DAY_OF_WEEK_WORD)) {
                    lexems.add(new Lexem(TimeToken.DAY_OF_WEEK, values.get(PatternBuilder.WEEKS_DAY_OF_WEEK_WORD)));
                }

                if (values.containsKey(PatternBuilder.SUFFIX_DAYS)) {
                    lexems.add(new Lexem(TimeToken.DAYS, values.get(PatternBuilder.SUFFIX_DAYS)));
                } else if (values.containsKey(PatternBuilder.PREFIX_DAYS)) {
                    lexems.add(new Lexem(TimeToken.DAYS, values.get(PatternBuilder.PREFIX_DAYS)));
                } else if (values.containsKey(PatternBuilder.ONE_DAY)) {
                    lexems.add(new Lexem(TimeToken.DAYS, "1"));
                }

                if (values.containsKey(PatternBuilder.SUFFIX_HOURS)) {
                    lexems.add(new Lexem(TimeToken.HOURS, values.get(PatternBuilder.SUFFIX_HOURS)));
                } else if (values.containsKey(PatternBuilder.PREFIX_HOURS)) {
                    lexems.add(new Lexem(TimeToken.HOURS, values.get(PatternBuilder.PREFIX_HOURS)));
                } else if (values.containsKey(PatternBuilder.ONE_HOUR)) {
                    lexems.add(new Lexem(TimeToken.HOURS, "1"));
                }

                if (values.containsKey(PatternBuilder.SUFFIX_MINUTES)) {
                    lexems.add(new Lexem(TimeToken.MINUTES, values.get(PatternBuilder.SUFFIX_MINUTES)));
                } else if (values.containsKey(PatternBuilder.PREFIX_MINUTES)) {
                    lexems.add(new Lexem(TimeToken.MINUTES, values.get(PatternBuilder.PREFIX_MINUTES)));
                } else if (values.containsKey(PatternBuilder.ONE_MINUTE)) {
                    lexems.add(new Lexem(TimeToken.MINUTES, "1"));
                }

                if (values.containsKey(PatternBuilder.DAY_OF_WEEK_WORD)) {
                    lexems.add(new Lexem(TimeToken.DAY_OF_WEEK, values.get(PatternBuilder.DAY_OF_WEEK_WORD)));
                }
                if (values.containsKey(PatternBuilder.DAY)) {
                    lexems.add(new Lexem(TimeToken.DAY, values.get(PatternBuilder.DAY)));
                }
                if (values.containsKey(PatternBuilder.SERIES_TO_COMPLETE)) {
                    lexems.add(new Lexem(RepeatTimeToken.SERIES_TO_COMPLETE, values.get(PatternBuilder.SERIES_TO_COMPLETE)));
                }
                if (values.containsKey(PatternBuilder.MONTH_WORD)) {
                    lexems.add(new Lexem(TimeToken.MONTH_WORD, values.get(PatternBuilder.MONTH_WORD)));
                }
                if (values.containsKey(PatternBuilder.HOUR)) {
                    lexems.add(new Lexem(TimeToken.HOUR, values.get(PatternBuilder.HOUR)));
                    lexems.add(new Lexem(TimeToken.MINUTE, values.getOrDefault(PatternBuilder.MINUTE, "00")));
                }
            }

            return lexems;
        }

        return null;
    }

    public int end() {
        return matchEnd;
    }

    private List<Map<String, String>> getValues() {
        LinkedList<Map<String, String>> values = new LinkedList<>();
        String tmp = str;
        GroupMatcher matcher = lexerConfig.getRepeatTimePattern(locale).maxMatcher(tmp);

        while (matcher != null) {
            values.addFirst(matcher.values());
            matchEnd += matcher.end();

            tmp = tmp.substring(0, tmp.length() - matcher.end());
            String trimmed = tmp.trim();
            matchEnd += tmp.length() - trimmed.length();
            tmp = trimmed;

            if (tmp.isEmpty()) {
                break;
            }

            matcher = lexerConfig.getRepeatTimePattern(locale).maxMatcher(tmp);
        }
        if (!withoutRepeatWord) {
            if (fullMatch) {
                matcher = lexerConfig.getRepeatWordPattern(locale).matcher(tmp);

                if (!matcher.matches()) {
                    return null;
                }
            } else {
                matcher = lexerConfig.getRepeatWordPattern(locale).maxMatcher(tmp);
            }
            if (matcher != null) {
                matchEnd += matcher.end();
                return values;
            }

            return null;
        } else {
            return values;
        }
    }
}
