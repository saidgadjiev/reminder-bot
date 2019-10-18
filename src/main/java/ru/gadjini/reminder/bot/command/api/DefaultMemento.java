package ru.gadjini.reminder.bot.command.api;

public class DefaultMemento implements CommandMemento {

    private final long chatId;

    private NavigableBotCommand botCommand;

    public DefaultMemento(long chatId, NavigableBotCommand botCommand) {
        this.chatId = chatId;
        this.botCommand = botCommand;
    }

    @Override
    public NavigableBotCommand getOriginator() {
        return botCommand;
    }

    public long getChatId() {
        return chatId;
    }
}
