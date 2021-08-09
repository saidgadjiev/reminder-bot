package ru.gadjini.reminder.domain;

import java.time.ZonedDateTime;

public class Goal {

    public static final String ID = "id";

    public static final String TITLE = "title";

    public static final String DESCRIPTION = "description";

    public static final String USER_ID = "user_id";

    public static final String TARGET_DATE = "target_date";

    public static final String CREATED_AT = "created_at";

    public static final String GOAL_ID = "goal_id";

    private int id;

    private String title;

    private String description;

    private ZonedDateTime targetDate;

    private ZonedDateTime createdAt;

    private long userId;

    private int goalId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(ZonedDateTime targetDate) {
        this.targetDate = targetDate;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }
}
