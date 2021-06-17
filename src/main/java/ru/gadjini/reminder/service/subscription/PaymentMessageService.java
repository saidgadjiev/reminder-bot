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

    public Integer getMessageId(long userId) {
        return paymentMessageDao.getMessageId(userId);
    }

    public void create(long userId, int messageId) {
        paymentMessageDao.create(userId, messageId);
    }

    public Integer delete(long userId) {
        return paymentMessageDao.delete(userId);
    }
}
