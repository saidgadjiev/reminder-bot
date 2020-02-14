package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RepeatTimeLexer {

    private TimeLexerConfig lexerConfig;

    private String str;

    private boolean fullMatch;

    private final Locale locale;

    private int matchEnd;

    public RepeatTimeLexer(TimeLexerConfig lexerConfig, String str, boolean fullMatch, Locale locale) {
        this.lexerConfig = lexerConfig;
        this.str = str;
        this.fullMatch = fullMatch;
        this.locale = locale;
    }

    public LinkedList<BaseLexem> tokenize() {
        LinkedList<BaseLexem> lexems = new LinkedList<>();

        List<Map<String, String>> valuesList = getValues();

        if (valuesList != null) {
            for (Map<String, String> values : valuesList) {
                if (values.containsKey(PatternBuilder.SUFFIX_YEARS)) {
                    lexems.add(new TimeLexem(TimeToken.YEARS, values.get(PatternBuilder.SUFFIX_YEARS)));
                } else if (values.containsKey(PatternBuilder.PREFIX_YEARS)) {
                    lexems.add(new TimeLexem(TimeToken.YEARS, values.get(PatternBuilder.PREFIX_YEARS)));
                } else if (values.containsKey(PatternBuilder.ONE_YEAR)) {
                    lexems.add(new TimeLexem(TimeToken.YEARS, "1"));
                }

                if (values.containsKey(PatternBuilder.SUFFIX_MONTHS)) {
                    lexems.add(new TimeLexem(TimeToken.MONTHS, values.get(PatternBuilder.SUFFIX_MONTHS)));
                } else if (values.containsKey(PatternBuilder.PREFIX_MONTHS)) {
                    lexems.add(new TimeLexem(TimeToken.MONTHS, values.get(PatternBuilder.PREFIX_MONTHS)));
                } else if (values.containsKey(PatternBuilder.ONE_MONTH)) {
                    lexems.add(new TimeLexem(TimeToken.MONTHS, "1"));
                }

                if (values.containsKey(PatternBuilder.PREFIX_DAY_OF_MONTH)) {
                    lexems.add(new TimeLexem(TimeToken.DAY, values.get(PatternBuilder.PREFIX_DAY_OF_MONTH)));
                } else if (values.containsKey(PatternBuilder.SUFFIX_DAY_OF_MONTH)) {
                    lexems.add(new TimeLexem(TimeToken.DAY, values.get(PatternBuilder.SUFFIX_DAY_OF_MONTH)));
                }

                if (values.containsKey(PatternBuilder.SUFFIX_WEEKS)) {
                    lexems.add(new TimeLexem(TimeToken.WEEKS, values.get(PatternBuilder.SUFFIX_WEEKS)));
                } else if (values.containsKey(PatternBuilder.PREFIX_WEEKS)) {
                    lexems.add(new TimeLexem(TimeToken.WEEKS, values.get(PatternBuilder.PREFIX_WEEKS)));
                } else if (values.containsKey(PatternBuilder.ONE_WEEK)) {
                    lexems.add(new TimeLexem(TimeToken.WEEKS, "1"));
                }

                if (values.containsKey(PatternBuilder.WEEKS_DAY_OF_WEEK_WORD)) {
                    lexems.add(new TimeLexem(TimeToken.DAY_OF_WEEK, values.get(PatternBuilder.WEEKS_DAY_OF_WEEK_WORD)));
                }

                if (values.containsKey(PatternBuilder.SUFFIX_DAYS)) {
                    lexems.add(new TimeLexem(TimeToken.DAYS, values.get(PatternBuilder.SUFFIX_DAYS)));
                } else if (values.containsKey(PatternBuilder.PREFIX_DAYS)) {
                    lexems.add(new TimeLexem(TimeToken.DAYS, values.get(PatternBuilder.PREFIX_DAYS)));
                } else if (values.containsKey(PatternBuilder.ONE_DAY)) {
                    lexems.add(new TimeLexem(TimeToken.DAYS, "1"));
                }

                if (values.containsKey(PatternBuilder.SUFFIX_HOURS)) {
                    lexems.add(new TimeLexem(TimeToken.HOURS, values.get(PatternBuilder.SUFFIX_HOURS)));
                } else if (values.containsKey(PatternBuilder.PREFIX_HOURS)) {
                    lexems.add(new TimeLexem(TimeToken.HOURS, values.get(PatternBuilder.PREFIX_HOURS)));
                } else if (values.containsKey(PatternBuilder.ONE_HOUR)) {
                    lexems.add(new TimeLexem(TimeToken.HOURS, "1"));
                }

                if (values.containsKey(PatternBuilder.SUFFIX_MINUTES)) {
                    lexems.add(new TimeLexem(TimeToken.MINUTES, values.get(PatternBuilder.SUFFIX_MINUTES)));
                } else if (values.containsKey(PatternBuilder.PREFIX_MINUTES)) {
                    lexems.add(new TimeLexem(TimeToken.MINUTES, values.get(PatternBuilder.PREFIX_MINUTES)));
                } else if (values.containsKey(PatternBuilder.ONE_MINUTE)) {
                    lexems.add(new TimeLexem(TimeToken.MINUTES, "1"));
                }

                if (values.containsKey(PatternBuilder.DAY_OF_WEEK_WORD)) {
                    lexems.add(new TimeLexem(TimeToken.DAY_OF_WEEK, values.get(PatternBuilder.DAY_OF_WEEK_WORD)));
                }
                if (values.containsKey(PatternBuilder.DAY)) {
                    lexems.add(new TimeLexem(TimeToken.DAY, values.get(PatternBuilder.DAY)));
                }
                if (values.containsKey(PatternBuilder.MONTH_WORD)) {
                    lexems.add(new TimeLexem(TimeToken.MONTH_WORD, values.get(PatternBuilder.MONTH_WORD)));
                }

                if (values.containsKey(PatternBuilder.HOUR)) {
                    lexems.add(new TimeLexem(TimeToken.HOUR, values.get(PatternBuilder.HOUR)));
                    lexems.add(new TimeLexem(TimeToken.MINUTE, values.getOrDefault(PatternBuilder.MINUTE, "00")));
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
    }
}
