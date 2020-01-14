package ru.gadjini.reminder.bot.command.callback;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandNavigator;

@Component
public class GoBackCallbackCommand implements CallbackBotCommand {

    private CallbackCommandNavigator callbackCommandNavigator;

    private CommandNavigator commandNavigator;

    @Autowired
    public void setCallbackCommandNavigator(CallbackCommandNavigator callbackCommandNavigator) {
        this.callbackCommandNavigator = callbackCommandNavigator;
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return CommandNames.GO_BACK_CALLBACK_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        String prevCommandName = requestParams.getString(Arg.PREV_HISTORY_NAME.getKey());
        ReplyKeyboard replyKeyboard = null;

        int code = requestParams.getInt(Arg.RESTORE_KEYBOARD.getKey());
        RestoreKeyboard restoreKeyboard = RestoreKeyboard.fromCode(code);

        switch (restoreKeyboard) {
            case RESTORE_KEYBOARD:
                replyKeyboard = commandNavigator.silentPop(callbackQuery.getMessage().getChatId());
                break;
            case RESTORE_HISTORY:
                commandNavigator.silentPop(callbackQuery.getMessage().getChatId());
                break;
            case NONE:
                break;
        }
        callbackCommandNavigator.goTo(TgMessage.from(callbackQuery), prevCommandName, replyKeyboard, requestParams);
        return null;
    }

    public enum RestoreKeyboard {

        RESTORE_KEYBOARD(0),

        RESTORE_HISTORY(1),

        NONE(2);

        private final int code;

        RestoreKeyboard(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static RestoreKeyboard fromCode(int code) {
            for (RestoreKeyboard value: values()) {
                if (value.code == code) {
                    return value;
                }
            }

            throw new IllegalArgumentException();
        }
    }
}
