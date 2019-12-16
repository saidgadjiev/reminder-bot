package ru.gadjini.reminder.service.parser.remind.parser;

import java.time.LocalTime;

public class OffsetTime {

    private Type type;

    private int days;

    private int hours;

    private int minutes;

    private LocalTime time;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public enum Type {

        AFTER,

        BEFORE
    }
}
