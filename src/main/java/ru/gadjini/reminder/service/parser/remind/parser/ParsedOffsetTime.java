package ru.gadjini.reminder.service.parser.remind.parser;

public class ParsedOffsetTime {

    private Type type;

    private Integer hour;

    private Integer minute;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public enum Type {

        AFTER,

        BEFORE
    }
}
