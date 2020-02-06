package ru.gadjini.reminder.service.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.command.state.CommandStateDao;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;

@Service
public class CommandStateService {

    private CommandStateDao commandStateDao;

    private LocalisationService localisationService;

    private TgUserService userService;

    @Autowired
    public CommandStateService(@Qualifier("redis") CommandStateDao commandStateDao, LocalisationService localisationService, TgUserService userService) {
        this.commandStateDao = commandStateDao;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    public void setState(long chatId, Object state) {
        commandStateDao.setState(chatId, state);
    }

    public <T> T getState(long chatId, boolean expiredCheck) {
        T state = commandStateDao.getState(chatId);

        if (expiredCheck && state == null) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_SESSION_EXPIRED, userService.getLocale((int) chatId)));
        }

        return state;
    }

    public void deleteState(long chatId) {
        commandStateDao.deleteState(chatId);
    }
}
