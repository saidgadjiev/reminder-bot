package ru.gadjini.reminder.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.gadjini.reminder.request.RequestParams;

@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE)
public class CallbackRequest {

    private int messageId;

    private RequestParams requestParams;

    @JsonCreator
    public CallbackRequest(@JsonProperty("messageId") int messageId, @JsonProperty("requestParams") RequestParams requestParams) {
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
