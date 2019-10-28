package ru.gadjini.reminder.service.resolver.parser;

import java.time.LocalTime;

public class ParsedTime {

    private LocalTime time;

    private Integer addDays;

    private Integer day;

    public LocalTime getTime() {
        return time;
    }

    void setTime(LocalTime time) {
        this.time = time;
    }

    public Integer getAddDays() {
        return addDays;
    }

    void setAddDays(Integer addDays) {
        this.addDays = addDays;
    }

    public Integer getDay() {
        return day;
    }

    void setDay(Integer day) {
        this.day = day;
    }
}
