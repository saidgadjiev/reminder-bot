package ru.gadjini.reminder.service.ai;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class MessageSenderAI {

    public boolean isNeedSendAction(ExecutionContext executionContext, ActionType actionType) {
        if (actionType == null) {
            return false;
        }
        if (executionContext.command && executionContext.message.hasVoice()) {
            return false;
        }

        return actionType == ActionType.TYPING;
    }

    public static class ExecutionContext {

        private boolean command;

        private Message message;

        public ExecutionContext command(final boolean command) {
            this.command = command;
            return this;
        }

        public ExecutionContext update(final Message update) {
            this.message = update;
            return this;
        }
    }
}
