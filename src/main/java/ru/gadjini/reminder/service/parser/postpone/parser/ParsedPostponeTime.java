package ru.gadjini.reminder.service.parser.postpone.parser;

import java.time.ZonedDateTime;

public class ParsedPostponeTime {

    private PostponeOn postponeOn;

    private ZonedDateTime postponeAt;

    public PostponeOn getPostponeOn() {
        return postponeOn;
    }

    public void setPostponeOn(PostponeOn postponeOn) {
        this.postponeOn = postponeOn;
    }

    public ZonedDateTime getPostponeAt() {
        return postponeAt;
    }

    public void setPostponeAt(ZonedDateTime postponeAt) {
        this.postponeAt = postponeAt;
    }

    public enum Type {

        ON,

        AT
    }

    @Override
    public String toString() {
        return "ParsedPostponeTime{" +
                "postponeOn=" + postponeOn +
                ", postponeAt=" + postponeAt +
                '}';
    }
}
