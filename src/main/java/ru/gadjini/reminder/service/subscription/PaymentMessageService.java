package ru.gadjini.reminder.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.paymentmessage.PaymentMessageDao;

@Service
public class PaymentMessageService {

    private PaymentMessageDao paymentMessageDao;

    @Autowired
    public PaymentMessageService(PaymentMessageDao paymentMessageDao) {
        this.paymentMessageDao = paymentMessageDao;
    }

    public Integer getMessageId(int userId) {
        return paymentMessageDao.getMessageId(userId);
    }

    public void create(int userId, int messageId) {
        paymentMessageDao.create(userId, messageId);
    }

    public Integer delete(int userId) {
        return paymentMessageDao.delete(userId);
    }
}
