package ru.gadjini.reminder.service.savedquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.SavedQuery;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.List;

@Service
public class SavedQueryMessageBuilder {

    private LocalisationService localisationService;

    @Autowired
    public SavedQueryMessageBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public String getMessage(List<SavedQuery> queries) {
        if (queries.isEmpty()) {
            return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_SAVED_QUERY_EMPTY, null);
        }

        StringBuilder message = new StringBuilder();
        int i = 1;

        for (SavedQuery query : queries) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append("<b>").append(i++).append(")</b> ").append(query.getQuery());
        }

        return message.toString();
    }
}
