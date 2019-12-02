package ru.gadjini.reminder.model;

import ru.gadjini.reminder.request.RequestParams;

public class CallbackRequest {

    private int messageId;

    private RequestParams requestParams;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(RequestParams requestParams) {
        this.requestParams = requestParams;
    }
}
