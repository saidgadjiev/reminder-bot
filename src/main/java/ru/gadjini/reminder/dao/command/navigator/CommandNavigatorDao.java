package ru.gadjini.reminder.dao.command.navigator;

public interface CommandNavigatorDao {
    void set(long chatId, String command);

    String get(long chatId);

    void setParent(long chatId, String command);

    String getParent(long chatId, String defaultCommand);
}
