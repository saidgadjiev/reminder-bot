package ru.gadjini.reminder.exception;

public class UserMessageParseException extends RuntimeException {

    public UserMessageParseException() { }

    public UserMessageParseException(String message) {
        super(message);
    }
}
