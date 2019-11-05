package ru.gadjini.reminder.service.parser.postpone.parser;

public class PostponeOn {

    private Integer day;

    private Integer hour;

    private Integer minute;

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    @Override
    public String toString() {
        return "PostponeOn{" +
                "day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                '}';
    }
}
