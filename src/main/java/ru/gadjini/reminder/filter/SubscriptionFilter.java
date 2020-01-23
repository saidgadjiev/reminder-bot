package ru.gadjini.reminder.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.ReminderConstants;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.domain.Subscription;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.properties.SubscriptionProperties;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.declension.TimeDeclensionService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.subscription.PaymentMessageService;
import ru.gadjini.reminder.service.subscription.PlanService;
import ru.gadjini.reminder.service.subscription.SubscriptionService;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class SubscriptionFilter extends BaseBotFilter {

    private MessageService messageService;

    private LocalisationService localisationService;

    private SubscriptionService subscriptionService;

    private PlanService planService;

    private ReplyKeyboardService replyKeyboardService;

    private InlineKeyboardService inlineKeyboardService;

    private PaymentMessageService paymentMessageService;

    private SubscriptionProperties subscriptionProperties;

    private Map<String, TimeDeclensionService> declensionServiceMap = new HashMap<>();

    private CommandNavigator commandNavigator;

    private TimeCreator timeCreator;

    @Autowired
    public SubscriptionFilter(MessageService messageService,
                              LocalisationService localisationService, SubscriptionService subscriptionService,
                              PlanService planService, CurrReplyKeyboard replyKeyboardService,
                              InlineKeyboardService inlineKeyboardService, PaymentMessageService paymentMessageService,
                              SubscriptionProperties subscriptionProperties, Collection<TimeDeclensionService> declensionServices,
                              CommandNavigator commandNavigator, TimeCreator timeCreator) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.subscriptionService = subscriptionService;
        this.planService = planService;
        this.replyKeyboardService = replyKeyboardService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.paymentMessageService = paymentMessageService;
        this.subscriptionProperties = subscriptionProperties;
        this.commandNavigator = commandNavigator;
        this.timeCreator = timeCreator;
        declensionServices.forEach(timeDeclensionService -> declensionServiceMap.put(timeDeclensionService.getLanguage(), timeDeclensionService));
    }

    @Override
    public void doFilter(Update update) {
        int userId = TgMessage.getUserId(update);
        boolean checkSubscriptionResult = checkSubscription(userId);

        if (checkSubscriptionResult) {
            super.doFilter(update);
        }
    }

    private boolean checkSubscription(int userId) {
        Subscription subscription = subscriptionService.getSubscription(userId);

        if (subscription == null) {
            subscriptionService.createTrialSubscription(userId);
            sendTrialSubscriptionStarted(userId);
            commandNavigator.setCurrentCommand(userId, CommandNames.START_COMMAND_NAME);

            return false;
        }
        if (subscription.getEndDate().isBefore(timeCreator.localDateNow())) {
            sendSubscriptionExpired(userId);

            return false;
        }

        return true;
    }

    private void sendTrialSubscriptionStarted(int userId) {
        TimeDeclensionService declensionService = declensionServiceMap.get(Locale.getDefault().getLanguage());

        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(userId)
                        .text(
                                localisationService.getMessage(MessagesProperties.MESSAGE_TRIAL_PERIOD_STARTED,
                                        new Object[]{
                                                declensionService.day(subscriptionProperties.getTrialPeriod()),
                                                ZoneId.of(ReminderConstants.DEFAULT_TIMEZONE).getDisplayName(TextStyle.FULL, Locale.getDefault())
                                        })
                        ).replyKeyboard(replyKeyboardService.getMainMenu(userId, userId))
        );
    }

    private void sendSubscriptionExpired(int userId) {
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(userId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED))
                        .replyKeyboard(replyKeyboardService.removeKeyboard(userId))
        );

        Integer messageId = paymentMessageService.getMessageId(userId);
        if (messageId != null) {
            messageService.deleteMessage(userId, messageId);
        }

        Plan plan = planService.getActivePlan();
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(userId)
                        .text(getNeedPayMessage(plan.getDescription()))
                        .replyKeyboard(inlineKeyboardService.getPaymentKeyboard(userId, plan.getId())),
                message -> paymentMessageService.create(userId, message.getMessageId())
        );
    }

    private String getNeedPayMessage(String planDesc) {
        return planDesc + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_PAYMENT_TYPE);
    }
}
