package ru.gadjini.reminder.service.requestresolver.postpone.parser;

public class ParsedPostponeTime {

    private PostponeOn postponeOn;

    private PostponeAt postponeAt;

    public PostponeOn getPostponeOn() {
        return postponeOn;
    }

    public void setPostponeOn(PostponeOn postponeOn) {
        this.postponeOn = postponeOn;
    }

    public PostponeAt getPostponeAt() {
        return postponeAt;
    }

    public void setPostponeAt(PostponeAt postponeAt) {
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
