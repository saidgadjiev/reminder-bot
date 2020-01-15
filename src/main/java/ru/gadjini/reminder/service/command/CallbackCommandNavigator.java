package ru.gadjini.reminder.service.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.util.ReflectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class CallbackCommandNavigator {

    private Map<String, NavigableCallbackBotCommand> navigableBotCommands = new HashMap<>();

    private Map<Long, NavigableCallbackBotCommand> currCommands = new HashMap<>();

    private CommandNavigator commandNavigator;

    @Autowired
    public void setKeyboardCommands(Collection<KeyboardBotCommand> keyboardCommands) {
        ReflectionUtils.findImplements(keyboardCommands, NavigableCallbackBotCommand.class).forEach(command -> navigableBotCommands.put(command.getName(), command));
    }

    @Autowired
    public void setCallbackCommands(Collection<CallbackBotCommand> callbackCommands) {
        ReflectionUtils.findImplements(callbackCommands, NavigableCallbackBotCommand.class).forEach(command -> navigableBotCommands.put(command.getName(), command));
    }

    @Autowired
    public void setBotCommands(Collection<BotCommand> botCommands) {
        ReflectionUtils.findImplements(botCommands, NavigableCallbackBotCommand.class).forEach(command -> navigableBotCommands.put(command.getName(), command));
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    public void push(long chatId, NavigableCallbackBotCommand callbackBotCommand) {
        if (callbackBotCommand.isAcquireKeyboard()) {
            currCommands.put(chatId, callbackBotCommand);
        }
    }

    public void popTo(TgMessage message, String commandName, RequestParams requestParams) {
        ReplyKeyboard replyKeyboard = null;
        if (requestParams.contains(Arg.RESTORE_KEYBOARD.getKey())) {
            int code = requestParams.getInt(Arg.RESTORE_KEYBOARD.getKey());
            RestoreKeyboard restoreKeyboard = CallbackCommandNavigator.RestoreKeyboard.fromCode(code);
            if (restoreKeyboard == RestoreKeyboard.RESTORE_KEYBOARD) {
                replyKeyboard = commandNavigator.getCurrentCommand(message.getChatId()).getKeyboard(message.getChatId());
            }
        }

        NavigableCallbackBotCommand currCommand = currCommands.remove(message.getChatId());
        if (currCommand != null) {
            currCommand.leave(message.getChatId());
        }

        NavigableCallbackBotCommand callbackBotCommand = navigableBotCommands.get(commandName);
        callbackBotCommand.restore(message, replyKeyboard, requestParams);
    }

    public NavigableCallbackBotCommand getCurrentCommand(long chatId) {
        return currCommands.get(chatId);
    }

    public enum RestoreKeyboard {

        RESTORE_KEYBOARD(0);

        private final int code;

        RestoreKeyboard(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static RestoreKeyboard fromCode(int code) {
            for (RestoreKeyboard value : values()) {
                if (value.code == code) {
                    return value;
                }
            }

            throw new IllegalArgumentException();
        }
    }
}
