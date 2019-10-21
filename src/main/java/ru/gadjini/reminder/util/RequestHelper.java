package ru.gadjini.reminder.util;

import ru.gadjini.reminder.model.ReminderRequest;

import java.util.List;
import java.util.function.Function;

public class RequestHelper {

    private RequestHelper() {
    }

    public static ReminderRequest findCandidate(List<Function<String, ReminderRequest>> candidates, String text) {
        for (Function<String, ReminderRequest> requestExtractor : candidates) {
            ReminderRequest candidate = requestExtractor.apply(text);

            if (candidate != null) {
                return candidate;
            }
        }

        return null;
    }

}
