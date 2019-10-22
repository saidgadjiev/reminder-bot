package ru.gadjini.reminder.service.resolver;

import org.springframework.stereotype.Service;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.resolver.matcher.LoginTextTimeMatcher;
import ru.gadjini.reminder.service.resolver.matcher.TextTimeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class ReminderRequestResolver {

    private List<Function<String, ReminderRequest>> REQUEST_RESOLVERS = new ArrayList<>() {{
        add(new LoginTextTimeMatcher());
        add(new TextTimeMatcher());
    }};

    public ReminderRequest resolve(String text) {
        for (Function<String, ReminderRequest> requestExtractor : REQUEST_RESOLVERS) {
            ReminderRequest candidate = requestExtractor.apply(text);

            if (candidate != null) {
                return candidate;
            }
        }

        return null;
    }
}
