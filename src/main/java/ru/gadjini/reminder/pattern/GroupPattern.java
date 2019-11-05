package ru.gadjini.reminder.pattern;

import java.util.List;
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
}
