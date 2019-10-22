package ru.gadjini.reminder.service.resolver;

import org.springframework.stereotype.Service;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.resolver.matcher.LoginTextTimeMatcher;
import ru.gadjini.reminder.service.resolver.matcher.MatchType;
import ru.gadjini.reminder.service.resolver.matcher.TextTimeMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class ReminderRequestResolver {

    private Map<MatchType, List<Function<String, ReminderRequest>>> REQUEST_RESOLVERS = new LinkedHashMap<>() {{
        put(MatchType.LOGIN_TEXT_TIME, new ArrayList<>() {{
            add(new LoginTextTimeMatcher());
        }});
        put(MatchType.TEXT_TIME, new ArrayList<>() {{
            add(new TextTimeMatcher());
        }});
    }};

    public ReminderRequest resolve(String text) {
        for (Map.Entry<MatchType, List<Function<String, ReminderRequest>>> entry : REQUEST_RESOLVERS.entrySet()) {
            for (Function<String, ReminderRequest> matcher : entry.getValue()) {
                ReminderRequest candidate = matcher.apply(text);

                if (candidate != null) {
                    return candidate;
                }
            }
        }

        return null;
    }

    public ReminderRequest resolve(String text, MatchType matchType) {
        for (Function<String, ReminderRequest> matcher : REQUEST_RESOLVERS.get(matchType)) {
            ReminderRequest candidate = matcher.apply(text);

            if (candidate != null) {
                return candidate;
            }
        }

        return null;
    }
}
