package ru.gadjini.reminder.model;

import ru.gadjini.reminder.request.RequestParams;

public class CallbackRequest {

    private int messageId;

    private RequestParams requestParams;

    public CallbackRequest(int messageId, RequestParams requestParams) {
        this.messageId = messageId;
        this.requestParams = requestParams;
    }

    public int getMessageId() {
        return messageId;
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }
}
