package ru.gadjini.reminder.pattern;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class GroupMatcher {

    private List<String> groups;

    private Matcher matcher;

    GroupMatcher(List<String> groups, Matcher matcher) {
        this.groups = groups;
        this.matcher = matcher;
    }

    public boolean find() {
        return matcher.find();
    }

    public boolean matches() {
        return matcher.matches();
    }

    public Map<String, String> values() {
        Map<String, String> values = new LinkedHashMap<>();

        for (String group: groups) {
            String found = matcher.group(group);

            if (found != null) {
                values.put(group, found);
            }
        }

        return values;
    }

    public int end() {
        return matcher.end();
    }
}
