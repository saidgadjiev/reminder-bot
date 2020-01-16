package ru.gadjini.reminder.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.reminder.request.RequestParams;

@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE)
public class CallbackRequest {

    private int messageId;

    private RequestParams requestParams;

    private InlineKeyboardMarkup replyKeyboard;

    @JsonCreator
    public CallbackRequest(@JsonProperty("messageId") int messageId, @JsonProperty("requestParams") RequestParams requestParams, @JsonProperty("replyKeyboard") InlineKeyboardMarkup replyKeyboard) {
        this.messageId = messageId;
        this.requestParams = requestParams;
        this.replyKeyboard = replyKeyboard;
    }

    public int getMessageId() {
        return messageId;
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }

    public InlineKeyboardMarkup getReplyKeyboard() {
        return replyKeyboard;
    }
}
