package ru.gadjini.reminder.service.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.command.state.CommandStateDao;

@Service
public class CommandStateService {

    private CommandStateDao commandStateDao;

    @Autowired
    public CommandStateService(@Qualifier("redis") CommandStateDao commandStateDao) {
        this.commandStateDao = commandStateDao;
    }

    public void setState(long chatId, Object state) {
        commandStateDao.setState(chatId, state);
    }

    public <T> T getState(long chatId) {
        return commandStateDao.getState(chatId);
    }

    public void deleteState(long chatId) {
        commandStateDao.deleteState(chatId);
    }
}
