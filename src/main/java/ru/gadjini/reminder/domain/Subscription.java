package ru.gadjini.reminder.domain;

import java.time.LocalDate;

public class Subscription {

    public static final String TABLE = "subscription";

    public static final String USER_ID = "user_id";

    public static final String END_DATE = "end_date";

    public static final String PLAN_ID = "plan_id";

    private int userId;

    private Integer planId;

    private LocalDate endDate;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }
}
