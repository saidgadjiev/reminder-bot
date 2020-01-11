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

    public Integer getMessageId(long chatId) {
        return paymentMessageDao.getMessageId(chatId);
    }

    public void create(long chatId, int messageId) {
        paymentMessageDao.create(chatId, messageId);
    }

    public Integer delete(long chatId) {
        return paymentMessageDao.delete(chatId);
    }
}
