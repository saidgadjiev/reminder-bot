package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.bots.AbsSender;

public class DefaultMemento implements CommandMemento {

    private final AbsSender absSender;

    private final long chatId;

    private NavigableBotCommand botCommand;

    public DefaultMemento(AbsSender absSender, long chatId, NavigableBotCommand botCommand) {
        this.absSender = absSender;
        this.chatId = chatId;
        this.botCommand = botCommand;
    }

    @Override
    public NavigableBotCommand getOriginator() {
        return botCommand;
    }

    public AbsSender getAbsSender() {
        return absSender;
    }

    public long getChatId() {
        return chatId;
    }
}
