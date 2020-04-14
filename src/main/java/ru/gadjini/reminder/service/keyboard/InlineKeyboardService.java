package ru.gadjini.reminder.service.keyboard;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.PaymentType;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.properties.WebHookProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandParser;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class InlineKeyboardService {

    private LocalisationService localisationService;

    private ButtonFactory buttonFactory;

    private WebHookProperties webHookProperties;

    private static final String PAYMENT_API_PATH = "payment/pay";

    @Autowired
    public InlineKeyboardService(LocalisationService localisationService, ButtonFactory buttonFactory, WebHookProperties webHookProperties) {
        this.localisationService = localisationService;
        this.buttonFactory = buttonFactory;
        this.webHookProperties = webHookProperties;
    }

    public InlineKeyboardMarkup getOpenDetailsKeyboard(int reminderId, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();
        keyboard.add(List.of(buttonFactory.reminderDetails(reminderId, locale)));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getCancelMessagesKeyboard(String prevCommand, RequestParams requestParams, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();

        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_CANCEL_REMINDER_NO_TIME, locale), CommandNames.CANCEL_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.REASON.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_CANCEL_REMINDER_NO_TIME, locale)))
        ));
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_CANCEL_REMINDER_MEETING, locale), CommandNames.CANCEL_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.REASON.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_CANCEL_REMINDER_MEETING, locale)))
        ));
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_DESCRIPTION, locale), CommandNames.CANCEL_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.REASON.getKey(), localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_DESCRIPTION, locale)))
        ));

        keyboard.add(List.of(buttonFactory.goBackCallbackButton(prevCommand, requestParams, locale)));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getPostponeMessagesKeyboard(String prevCommand, RequestParams requestParams, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();

        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_REASON_MEETING, locale), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.REASON.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_REASON_MEETING, locale))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION, locale), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.REASON.getKey(), localisationService.getMessage(MessagesProperties.POSTPONE_REMINDER_COMMAND_DESCRIPTION, locale)))
        ));

        keyboard.add(List.of(buttonFactory.goBackCallbackButton(prevCommand, requestParams, locale)));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getPostponeKeyboard(boolean hasTime, String prevCommand, RequestParams requestParams, Locale locale) {
        if (hasTime) {
            return getPostponeKeyboardForHasTime(prevCommand, requestParams, locale);
        }

        return getPostponeKeyboardForWithoutTime(prevCommand, requestParams, locale);
    }

    public InlineKeyboardMarkup getCustomRemindKeyboard(String prevCommand, RequestParams requestParams, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_BEFORE_15_MIN, locale), CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME, new RequestParams().add(Arg.CUSTOM_REMIND_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_BEFORE_15_MIN, locale))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_BEFORE_30_MIN, locale), CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME, new RequestParams().add(Arg.CUSTOM_REMIND_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_BEFORE_30_MIN, locale))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_BEFORE_1_H, locale), CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME, new RequestParams().add(Arg.CUSTOM_REMIND_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_BEFORE_1_H, locale)))
        ));
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_30_MIN, locale), CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME, new RequestParams().add(Arg.CUSTOM_REMIND_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_30_MIN, locale))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_1_H, locale), CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME, new RequestParams().add(Arg.CUSTOM_REMIND_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_1_H, locale))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_2_H, locale), CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME, new RequestParams().add(Arg.CUSTOM_REMIND_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_2_H, locale)))
        ));
        keyboard.add(List.of(buttonFactory.goBackCallbackButton(prevCommand, requestParams, locale)));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getSavedQueriesKeyboard(List<Integer> queries) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        int i = 1;
        List<List<Integer>> lists = Lists.partition(queries, 4);
        for (List<Integer> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int id : list) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i++));
                button.setCallbackData(CommandNames.DELETE_SAVED_QUERY_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                        new RequestParams().add(Arg.SAVED_QUERY_ID.getKey(), id).serialize(CommandParser.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getPaymentKeyboard(int userId, int planId, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();
        keyboard.add(List.of(buttonFactory.paymentButton(localisationService.getMessage(MessagesProperties.PAYMENT_BEELINE_COMMAND_DESCRIPTION, locale), buildPayUrl(userId, planId, PaymentType.BEELINE))));
        keyboard.add(List.of(buttonFactory.paymentButton(localisationService.getMessage(MessagesProperties.PAYMENT_CARD_COMMAND_DESCRIPTION, locale), buildPayUrl(userId, planId, PaymentType.CARD))));
        keyboard.add(List.of(buttonFactory.paymentButton(localisationService.getMessage(MessagesProperties.PAYMENT_WEB_MONEY_DESCRIPTION, locale), buildPayUrl(userId, planId, PaymentType.WEB_MONEY))));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getUserReminderNotificationInlineKeyboard(List<Integer> reminderNotificationsIds, UserReminderNotification.NotificationType notificationType) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        int i = 1;
        List<List<Integer>> lists = Lists.partition(reminderNotificationsIds, 4);
        for (List<Integer> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int id : list) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i++));
                button.setCallbackData(CommandNames.DELETE_USER_REMINDER_NOTIFICATION_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                        new RequestParams()
                                .add(Arg.USER_REMINDER_NOTIFICATION_ID.getKey(), id)
                                .add(Arg.USER_REMINDER_NOTIFICATION_TYPE.getKey(), notificationType.getCode())
                                .serialize(CommandParser.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getReminderTimeKeyboard(int reminderTimeId, int reminderId, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderTimeButton(reminderTimeId, locale)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.SCHEDULE_COMMAND_NAME, new RequestParams().add(Arg.REMINDER_ID.getKey(), reminderId), locale)));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getReminderTimesListKeyboard(List<Integer> reminderTimesIds, int reminderId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        int i = 1;
        List<List<Integer>> lists = Lists.partition(reminderTimesIds, 4);
        for (List<Integer> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int reminderTimeId : list) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i++));
                button.setCallbackData(CommandNames.REMINDER_TIME_DETAILS_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                        new RequestParams()
                                .add(Arg.REMINDER_NOTIFICATION_ID.getKey(), reminderTimeId)
                                .serialize(CommandParser.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }
        if (reminderTimesIds.size() > 0) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.suppressNotifications(reminderId, locale)));
        }
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(reminderId, CommandNames.SCHEDULE_COMMAND_NAME, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.REMINDER_DETAILS_COMMAND_NAME, new RequestParams().add(Arg.REMINDER_ID.getKey(), reminderId), locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getChooseChallengeParticipantKeyboard(List<Integer> friendUserIds, Locale locale) {
        InlineKeyboardMarkup friendsListKeyboard = getFriendsListKeyboard(
                friendUserIds,
                CommandNames.CALLBACK_DELEGATE_COMMAND_NAME,
                new RequestParams()
                        .add(Arg.CALLBACK_DELEGATE.getKey(), CommandNames.CREATE_CHALLENGE_COMMAND_NAME)
        );

        friendsListKeyboard.getKeyboard().add(List.of(buttonFactory.delegateButton(
                localisationService.getMessage(MessagesProperties.GO_TO_NEXT_COMMAND_DESCRIPTION, locale),
                CommandNames.CREATE_CHALLENGE_COMMAND_NAME,
                new RequestParams().add(Arg.COMMAND_NAME.getKey(), CommandNames.GO_TO_NEXT_COMMAND_NAME))));

        return friendsListKeyboard;
    }

    public InlineKeyboardMarkup getFriendsListKeyboard(List<Integer> friendsUserIds, String commandName, RequestParams requestParams) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        int i = 1;
        List<List<Integer>> lists = Lists.partition(friendsUserIds, 4);
        for (List<Integer> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int friendUserId : list) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i++));
                button.setCallbackData(commandName + CommandParser.COMMAND_NAME_SEPARATOR +
                        new RequestParams()
                                .add(Arg.FRIEND_ID.getKey(), friendUserId)
                                .merge(requestParams == null ? new RequestParams() : requestParams)
                                .serialize(CommandParser.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getActiveRemindersListKeyboard(List<Integer> reminderIds, String prevHistoryName, RequestParams requestParams, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        int i = 1;
        List<List<Integer>> lists = Lists.partition(reminderIds, 4);
        for (List<Integer> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int remindId : list) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i++));
                button.setCallbackData(CommandNames.REMINDER_DETAILS_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                        new RequestParams()
                                .add(Arg.REMINDER_ID.getKey(), remindId)
                                .merge(requestParams)
                                .serialize(CommandParser.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(MessagesProperties.TODAY_ACTIVE_REMINDERS_COMMAND_DESCRIPTION, ReminderDao.Filter.TODAY, locale), buttonFactory.getActiveRemindersButton(MessagesProperties.ALL_ACTIVE_REMINDERS_COMMAND_DESCRIPTION, ReminderDao.Filter.ALL, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getCompletedRemindersListKeyboard(String prevHistoryName, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteCompletedRemindersButton(locale)));
        if (prevHistoryName != null) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, locale)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getEmptyRemindersListKeyboard(String prevHistoryName, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getEmptyActiveRemindersListKeyboard(String prevHistoryName, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(MessagesProperties.TODAY_ACTIVE_REMINDERS_COMMAND_DESCRIPTION, ReminderDao.Filter.TODAY, locale), buttonFactory.getActiveRemindersButton(MessagesProperties.ALL_ACTIVE_REMINDERS_COMMAND_DESCRIPTION, ReminderDao.Filter.ALL, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getCreatorReminderKeyboard(Reminder reminder) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.reminderDetails(reminder.getId(), reminder.getCreator().getLocale())));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getReceiverReminderKeyboard(Reminder reminder) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        if (reminder.isRepeatable()) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeRepeatReminderButton(reminder.getId(), reminder.getReceiver().getLocale()), buttonFactory.cancelReminderButton(reminder.getId(), reminder.getReceiver().getLocale())));
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.skipRepeatReminderButton(reminder.getId(), reminder.getReceiver().getLocale()), buttonFactory.stopRepeatReminderButton(reminder.getId(), reminder.getReceiver().getLocale())));
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME, reminder.getReceiver().getLocale())));
        } else {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeReminderButton(reminder.getId(), reminder.getReceiver().getLocale()), buttonFactory.cancelReminderButton(reminder.getId(), reminder.getReceiver().getLocale())));
            List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
            keyboardButtons.add(buttonFactory.customReminderTimeButton(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME, reminder.getReceiver().getLocale()));
            keyboardButtons.add(buttonFactory.postponeReminderButton(reminder.getId(), reminder.getReceiver().getLocale()));

            inlineKeyboardMarkup.getKeyboard().add(keyboardButtons);
        }
        if (!reminder.isSuppressNotifications()) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.suppressNotifications(reminder.getId(), reminder.getReceiver().getLocale())));
        }
        if (reminder.isNotMySelf() && reminder.isUnread()) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.readButton(reminder.getId(), reminder.getReceiver().getLocale())));
        }
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.reminderDetails(reminder.getId(), reminder.getReceiver().getLocale())));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.okButton(reminder.getId(), reminder.getReceiver().getLocale())));

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getReceiverReminderDetailsKeyboard(Reminder reminder, RequestParams requestParams) {
        if (reminder.isInactive()) {
            return null;
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = getInitialReceiverReminderDetailsKeyboard(reminder);

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME, requestParams, reminder.getReceiver().getLocale())));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getRemindersMenu(Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getCompletedRemindersButton(locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_DESCRIPTION, ReminderDao.Filter.ALL, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendKeyboard(int friendUserId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.createFriendReminderButton(friendUserId, locale),
                buttonFactory.deleteFriendButton(friendUserId, locale))
        );
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.changeFriendNameButton(friendUserId, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.goBackCallbackButton(CommandNames.GET_FRIENDS_COMMAND_HISTORY_NAME, locale)
        ));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendRequestKeyboard(int friendUserId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.acceptFriendRequestButton(friendUserId, locale),
                buttonFactory.rejectFriendRequestButton(friendUserId, locale)
        ));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(MessagesProperties.TO_ME_FRIEND_REQUESTS_COMMAND_NAME, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup goBackCallbackButton(String prevHistoryName, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getChallengeInvitation(int challengeId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.acceptChallenge(challengeId, locale), buttonFactory.rejectChallenge(challengeId, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.openChallengeDetails(challengeId, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup goBackCallbackButton(String prevHistoryName, CallbackCommandNavigator.RestoreKeyboard restoreKeyboard, RequestParams requestParams, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, requestParams.add(Arg.RESTORE_KEYBOARD.getKey(), restoreKeyboard.getCode()), locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup goBackCallbackButton(String prevHistoryName, RequestParams requestParams, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, requestParams, locale)));

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getCreatorReminderDetailsKeyboard(Reminder reminder, RequestParams requestParams) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();
        addControlReminderButtons(keyboardMarkup, requestParams, reminder);

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME, requestParams, reminder.getCreator().getLocale())));

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getMySelfReminderDetailsKeyboard(Reminder reminder, RequestParams requestParams) {
        InlineKeyboardMarkup keyboardMarkup = getInitialReceiverReminderDetailsKeyboard(reminder);

        addControlReminderButtons(keyboardMarkup, requestParams, reminder);

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME, requestParams, reminder.getReceiver().getLocale())));


        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getReminderDetailsKeyboard(int currUserId, RequestParams requestParams, Reminder reminder) {
        if (reminder.getCreatorId() == reminder.getReceiverId()) {
            return getMySelfReminderDetailsKeyboard(reminder, requestParams);
        } else if (currUserId == reminder.getReceiverId()) {
            return getReceiverReminderDetailsKeyboard(reminder, requestParams);
        } else {
            return getCreatorReminderDetailsKeyboard(reminder, requestParams);
        }
    }

    public InlineKeyboardMarkup getEditReminderKeyboard(int reminderId, String prevHistoryName, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminderTimeButton(reminderId, locale)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminderTextButton(reminderId, locale)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.changeReminderNote(reminderId, locale)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderNote(reminderId, locale)));

        if (prevHistoryName != null) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, new RequestParams().add(Arg.REMINDER_ID.getKey(), reminderId), locale)));
        }

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup inlineKeyboardMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.setKeyboard(new ArrayList<>());

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getInitialReceiverReminderDetailsKeyboard(Reminder reminder) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        if (reminder.isInactive()) {
            return keyboardMarkup;
        }

        int reminderId = reminder.getId();
        Locale locale = reminder.getReceiver().getLocale();
        if (reminder.isRepeatable()) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeRepeatReminderButton(reminderId, locale), buttonFactory.cancelReminderButton(reminderId, locale)));
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.skipRepeatReminderButton(reminderId, locale), buttonFactory.stopRepeatReminderButton(reminderId, locale)));
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.returnReminderButton(reminderId, locale), buttonFactory.customReminderTimeButton(reminderId, CommandNames.REMINDER_DETAILS_COMMAND_NAME, locale)));
        } else {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeReminderButton(reminderId, locale), buttonFactory.cancelReminderButton(reminderId, locale)));
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(reminderId, CommandNames.REMINDER_DETAILS_COMMAND_NAME, locale), buttonFactory.postponeReminderButton(reminderId, locale)));
        }
        if (!reminder.isSuppressNotifications()) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.suppressNotifications(reminderId, locale)));
        }
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.reminderTimesScheduleButton(reminderId, locale)));
        if (reminder.isNotMySelf() && reminder.isUnread()) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.readButton(reminder.getId(), locale)));
        }

        return keyboardMarkup;
    }

    private void addControlReminderButtons(InlineKeyboardMarkup keyboardMarkup, RequestParams requestParams, Reminder reminder) {
        Locale locale = reminder.getCreator().getLocale();
        if (reminder.isInactive()) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.activateReminderButton(reminder.getId(), requestParams, locale)));
        } else {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminder(reminder.getId(), locale)));
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deactivateReminderButton(reminder.getId(), requestParams, locale)));
            if (reminder.isRepeatable()) {
                if (reminder.isCountSeries()) {
                    keyboardMarkup.getKeyboard().add(List.of(buttonFactory.disableCountSeries(reminder.getId(), locale)));
                } else {
                    keyboardMarkup.getKeyboard().add(List.of(buttonFactory.enableCountSeries(reminder.getId(), locale)));
                }
            }
        }
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderButton(reminder.getId(), locale)));
    }

    private InlineKeyboardMarkup getPostponeKeyboardForWithoutTime(String prevCommand, RequestParams requestParams, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_D, locale), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_D, locale))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_2_D, locale), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_2_D, locale)))
        ));
        keyboard.add(List.of(buttonFactory.goBackCallbackButton(prevCommand, requestParams, locale)));

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getPostponeKeyboardForHasTime(String prevCommand, RequestParams requestParams, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_15_MIN, locale), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_15_MIN, locale))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_30_MIN, locale), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_30_MIN, locale))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_H, locale), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_H, locale)))
        ));
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_2_H, locale), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_2_H, locale))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_D, locale), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_D, locale)))
        ));
        keyboard.add(List.of(buttonFactory.goBackCallbackButton(prevCommand, requestParams, locale)));

        return keyboardMarkup;
    }

    private String buildPayUrl(int userId, int planId, PaymentType paymentType) {
        return UriComponentsBuilder.fromHttpUrl(webHookProperties.getExternalUrl())
                .path(PAYMENT_API_PATH)
                .queryParam("planId", planId)
                .queryParam("userId", userId)
                .queryParam("paymentType", paymentType.getType())
                .toUriString();
    }
}
