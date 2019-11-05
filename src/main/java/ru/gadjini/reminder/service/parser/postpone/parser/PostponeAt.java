package ru.gadjini.reminder.service.parser.postpone.parser;

import java.time.LocalTime;

public class PostponeAt {

    private LocalTime time;

    private Integer month;

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

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    @Override
    public String toString() {
        return "PostponeAt{" +
                "time=" + time +
                ", month=" + month +
                ", addDays=" + addDays +
                ", day=" + day +
                '}';
    }
}
