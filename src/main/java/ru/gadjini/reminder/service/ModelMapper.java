package ru.gadjini.reminder.service;

import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import ru.gadjini.reminder.domain.TgUser;

import java.util.ArrayList;
import java.util.List;

@Service
public class ModelMapper {

    public List<InlineQueryResult> convert(List<TgUser> users) {
        List<InlineQueryResult> inlineQueryResults = new ArrayList<>();

        for (TgUser tgUser: users) {
            inlineQueryResults.add(convert(tgUser));
        }

        return inlineQueryResults;
    }

    public InlineQueryResult convert(TgUser user) {
        InlineQueryResultArticle resultArticle = new InlineQueryResultArticle();

        resultArticle.setId(String.valueOf(user.getId()));
        resultArticle.setTitle(TgUser.USERNAME_START + user.getUsername());

        InputTextMessageContent inputTextMessageContent = new InputTextMessageContent();

        inputTextMessageContent.setMessageText(user.getUsername());
        inputTextMessageContent.disableWebPagePreview();

        resultArticle.setInputMessageContent(inputTextMessageContent);

        return resultArticle;
    }
}
