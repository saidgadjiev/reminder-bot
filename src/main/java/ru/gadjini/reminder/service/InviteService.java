package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.InviteDao;

import java.util.UUID;

@Service
public class InviteService {

    private InviteDao inviteDao;

    @Autowired
    public InviteService(InviteDao inviteDao) {
        this.inviteDao = inviteDao;
    }

    public String delete(String token) {
        return inviteDao.delete(token);
    }

    public String createInvite() {
        String token = UUID.randomUUID().toString();

        inviteDao.create(token);

        return token;
    }
}
