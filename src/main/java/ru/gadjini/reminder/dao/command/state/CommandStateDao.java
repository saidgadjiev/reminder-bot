package ru.gadjini.reminder.dao.command.state;

public interface CommandStateDao {
    void setState(long chatId, Object state);

    <T> T getState(long chatId);

    void deleteState(long chatId);
}
