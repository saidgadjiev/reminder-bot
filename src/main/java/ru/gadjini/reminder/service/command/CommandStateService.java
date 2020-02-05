package ru.gadjini.reminder.service.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.command.state.CommandStateDao;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;

@Service
public class CommandStateService {

    private CommandStateDao commandStateDao;

    private LocalisationService localisationService;

    @Autowired
    public CommandStateService(@Qualifier("redis") CommandStateDao commandStateDao, LocalisationService localisationService) {
        this.commandStateDao = commandStateDao;
        this.localisationService = localisationService;
    }

    public void setState(long chatId, Object state) {
        commandStateDao.setState(chatId, state);
    }

    public <T> T getState(long chatId, boolean expiredCheck) {
        T state = commandStateDao.getState(chatId);

        if (expiredCheck && state == null) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_SESSION_EXPIRED));
        }

        return state;
    }

    public void deleteState(long chatId) {
        commandStateDao.deleteState(chatId);
    }
}
