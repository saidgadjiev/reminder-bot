package ru.gadjini.reminder.model;

public class AnswerCallbackContext {

    private String queryId;

    private String text;

    public String queryId() {
        return this.queryId;
    }

    public String text() {
        return this.text;
    }

    public AnswerCallbackContext queryId(final String queryId) {
        this.queryId = queryId;
        return this;
    }

    public AnswerCallbackContext text(final String text) {
        this.text = text;
        return this;
    }
}
