package ru.gadjini.reminder.service.suggestion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.SuggestionDao;

import java.util.List;

@Service
public class SuggestionService {

    private static final int LIMIT = 5;

    private SuggestionDao suggestionDao;

    @Autowired
    public SuggestionService(SuggestionDao suggestionDao) {
        this.suggestionDao = suggestionDao;
    }

    public List<String> getSuggestions(int userId) {
        return suggestionDao.getSuggestions(userId, LIMIT);
    }

    public void addSuggestion(int userId, String text) {
        suggestionDao.addSuggest(userId, text);
    }
}
