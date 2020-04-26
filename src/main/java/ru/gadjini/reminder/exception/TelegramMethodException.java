package ru.gadjini.reminder.exception;

import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class TelegramMethodException extends RuntimeException {

    private int errorCode;

    private final String response;

    private final long chatId;

    public TelegramMethodException(TelegramApiRequestException apiException, long chatId) {
        super(apiException.getApiResponse() + "(" + chatId + ")", apiException);
        this.errorCode = apiException.getErrorCode();
        this.response = apiException.getApiResponse();
        this.chatId = chatId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getResponse() {
        return response;
    }

    public long getChatId() {
        return chatId;
    }
}
