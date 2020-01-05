package ru.gadjini.reminder.domain;

import org.joda.time.Period;

public class Plan {

    public static final String ID = "id";

    public static final String DESCRIPTION = "description";

    public static final String PRICE = "price";

    public static final String PERIOD = "period";

    public static final String ACTIVE = "active";

    private int id;

    private String description;

    private int price;

    private Period period;

    private boolean active;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
