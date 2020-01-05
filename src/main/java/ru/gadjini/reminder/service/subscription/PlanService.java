package ru.gadjini.reminder.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.subscription.PlanDao;
import ru.gadjini.reminder.domain.Plan;

@Service
public class PlanService {

    private PlanDao planDao;

    @Autowired
    public PlanService(PlanDao planDao) {
        this.planDao = planDao;
    }

    public Plan getActivePlan() {
        return planDao.getPlan(true);
    }

    public Plan getPlan(int planId) {
        return planDao.getById(planId);
    }
}
