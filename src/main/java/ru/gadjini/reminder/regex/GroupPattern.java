package ru.gadjini.reminder.regex;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupPattern {

    private Pattern pattern;

    private List<String> groups;

    public GroupPattern(Pattern pattern, List<String> groups) {
        this.pattern = pattern;
        this.groups = groups;
    }

    public GroupMatcher matcher(String text) {
        return new GroupMatcher(groups, pattern.matcher(text));
    }

    public GroupMatcher maxMatcher(String text) {
        GroupMatcher fortuneMatcher = checkFortune(text);
        if (fortuneMatcher != null) {
            return fortuneMatcher;
        }

        return findMaxMatcher(text);
    }

    private GroupMatcher findMaxMatcher(String text) {
        String[] words = text.split(" ");
        StringBuilder toMatch = new StringBuilder();
        Matcher maxMatcher = null;
        for (int i = words.length - 1; i >= 0; --i) {
            if (toMatch.length() > 0) {
                toMatch.insert(0, " ");
            }
            toMatch.insert(0, words[i]);
            Matcher matcher = pattern.matcher(toMatch.toString());

            if (matcher.matches()) {
                maxMatcher = matcher;
            }
        }

        return maxMatcher == null ? null : new GroupMatcher(groups, maxMatcher);
    }

    private GroupMatcher checkFortune(String text) {
        Matcher matcher = pattern.matcher(text);

        return matcher.matches() ? new GroupMatcher(groups, matcher) : null;
    }
}
