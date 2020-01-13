package ru.gadjini.reminder.service.savedquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.SavedQueryDao;
import ru.gadjini.reminder.domain.SavedQuery;

import java.util.List;

@Service
public class SavedQueryService {

    private SavedQueryDao savedQueryDao;

    @Autowired
    public SavedQueryService(SavedQueryDao savedQueryDao) {
        this.savedQueryDao = savedQueryDao;
    }

    public List<SavedQuery> getQueries(int userId) {
        return savedQueryDao.getQueries(userId);
    }

    public List<String> getQueriesOnly(int userId) {
        return savedQueryDao.getQueriesOnly(userId);
    }

    public void saveQuery(int userId, String query) {
        savedQueryDao.saveQuery(userId, query);
    }

    public void delete(int queryId) {
        savedQueryDao.delete(queryId);
    }
}
