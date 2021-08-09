package ru.gadjini.reminder.service.goal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.GoalDao;
import ru.gadjini.reminder.domain.Goal;

import java.util.List;

@Service
public class GoalService {

    private GoalDao goalDao;

    @Autowired
    public GoalService(GoalDao goalDao) {
        this.goalDao = goalDao;
    }

    public void createGoal(Goal goal) {
        goalDao.create(goal);
    }

    public List<Goal> getGoals(long userId) {
        return goalDao.getGoals(userId);
    }

    public List<Goal> getGoals(long userId, int goalId) {
        return goalDao.getGoals(userId, goalId);
    }

    public Goal getGoal(int id) {
        return goalDao.getGoal(id);
    }
}
