package ru.gadjini.reminder.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.ReminderConstants;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.domain.Subscription;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.properties.SubscriptionProperties;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.declension.TimeDeclensionProvider;
import ru.gadjini.reminder.service.declension.TimeDeclensionService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.subscription.PaymentMessageService;
import ru.gadjini.reminder.service.subscription.PlanService;
import ru.gadjini.reminder.service.subscription.SubscriptionService;
import ru.gadjini.reminder.util.DateTimeService;

import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

//@Component
public class SubscriptionFilter extends BaseBotFilter {

    private MessageService messageService;

    private LocalisationService localisationService;

    private SubscriptionService subscriptionService;

    private PlanService planService;

    private ReplyKeyboardService replyKeyboardService;

    private InlineKeyboardService inlineKeyboardService;

    private PaymentMessageService paymentMessageService;

    private SubscriptionProperties subscriptionProperties;

    private TimeDeclensionProvider timeDeclensionProvider;

    private CommandNavigator commandNavigator;

    private DateTimeService timeCreator;

    private TgUserService userService;

    @Autowired
    public SubscriptionFilter(MessageService messageService,
                              LocalisationService localisationService, SubscriptionService subscriptionService,
                              PlanService planService, CurrReplyKeyboard replyKeyboardService,
                              InlineKeyboardService inlineKeyboardService, PaymentMessageService paymentMessageService,
                              SubscriptionProperties subscriptionProperties, TimeDeclensionProvider timeDeclensionProvider,
                              CommandNavigator commandNavigator, DateTimeService timeCreator, TgUserService userService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.subscriptionService = subscriptionService;
        this.planService = planService;
        this.replyKeyboardService = replyKeyboardService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.paymentMessageService = paymentMessageService;
        this.subscriptionProperties = subscriptionProperties;
        this.timeDeclensionProvider = timeDeclensionProvider;
        this.commandNavigator = commandNavigator;
        this.timeCreator = timeCreator;
        this.userService = userService;
    }

    @Override
    public void doFilter(Update update) {
        User user = TgMessage.getUser(update);
        boolean checkSubscriptionResult = checkSubscription(user);

        if (checkSubscriptionResult) {
            super.doFilter(update);
        }
    }

    private boolean checkSubscription(User user) {
        int userId = user.getId();
        Subscription subscription = subscriptionService.getSubscription(userId);

        if (subscription == null) {
            subscriptionService.createTrialSubscription(userId);
            sendTrialSubscriptionStarted(user);
            commandNavigator.setCurrentCommand(userId, CommandNames.START_COMMAND_NAME);

            return false;
        }
        if (subscription.getEndDate().isBefore(timeCreator.localDateNow())) {
            sendSubscriptionExpired(userId);

            return false;
        }

        return true;
    }

    private void sendTrialSubscriptionStarted(User user) {
        Locale locale = userService.getLocale(user.getId());
        TimeDeclensionService declensionService = timeDeclensionProvider.getService(locale.getLanguage());

        int userId = user.getId();
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(userId)
                        .text(
                                localisationService.getMessage(MessagesProperties.MESSAGE_TRIAL_PERIOD_STARTED,
                                        new Object[]{
                                                declensionService.day(subscriptionProperties.getTrialPeriod()),
                                                ZoneId.of(ReminderConstants.DEFAULT_TIMEZONE).getDisplayName(TextStyle.FULL, locale)
                                        }, locale)
                        ).replyKeyboard(replyKeyboardService.getMainMenu(userId, locale))
        );
    }

    private void sendSubscriptionExpired(int userId) {
        Locale locale = userService.getLocale(userId);
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(userId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED, locale))
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
                        .text(getNeedPayMessage(plan.getDescription(), locale))
                        .replyKeyboard(inlineKeyboardService.getPaymentKeyboard(userId, plan.getId(), locale)),
                message -> paymentMessageService.create(userId, message.getMessageId())
        );
    }

    private String getNeedPayMessage(String planDesc, Locale locale) {
        return planDesc + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_PAYMENT_TYPE, locale);
    }
}
