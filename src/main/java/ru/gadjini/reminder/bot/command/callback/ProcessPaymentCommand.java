package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.payment.PaymentService;

import java.time.LocalDate;

@Component
public class ProcessPaymentCommand implements CallbackBotCommand {

    private PaymentService paymentService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    @Autowired
    public ProcessPaymentCommand(PaymentService paymentService, MessageService messageService, ReplyKeyboardService replyKeyboardService) {
        this.paymentService = paymentService;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
    }

    @Override
    public String getName() {
        return CommandNames.PROCESS_PAYMENT_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());

        LocalDate localDate = paymentService.processPayment(callbackQuery.getFrom().getId(), requestParams.getInt(Arg.PLAN_ID.getKey()));

        messageService.sendMessageByCode(
                callbackQuery.getMessage().getChatId(),
                MessagesProperties.MESSAGE_SUBSCRIPTION_RENEWED,
                new Object[]{localDate},
                replyKeyboardService.getMainMenu()
        );
    }
}
