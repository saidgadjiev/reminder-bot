package ru.gadjini.reminder.service.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.resolver.matcher.MatchType;
import ru.gadjini.reminder.service.resolver.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReminderRequestResolver {

    private Map<MatchType, List<RequestMatcher>> requestMatchers = new LinkedHashMap<>();

    @Autowired
    public ReminderRequestResolver(List<RequestMatcher> requestMatchers) {
        requestMatchers.forEach(requestMatcher -> {
            this.requestMatchers.putIfAbsent(requestMatcher.getType(), new ArrayList<>());
            this.requestMatchers.get(requestMatcher.getType()).add(requestMatcher);
        });
    }

    public ReminderRequest resolve(String text) {
        for (Map.Entry<MatchType, List<RequestMatcher>> entry : requestMatchers.entrySet()) {
            for (RequestMatcher matcher : entry.getValue()) {
                ReminderRequest candidate = matcher.match(text);

                if (candidate != null) {
                    return candidate;
                }
            }
        }

        return null;
    }

    public ReminderRequest resolve(String text, MatchType matchType) {
        for (RequestMatcher matcher : requestMatchers.get(matchType)) {
            ReminderRequest candidate = matcher.match(text);

            if (candidate != null) {
                return candidate;
            }
        }

        return null;
    }
}
