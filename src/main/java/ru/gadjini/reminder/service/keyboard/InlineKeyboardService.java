package ru.gadjini.reminder.service.keyboard;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.PaymentType;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.properties.WebHookProperties;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandParser;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.ReminderService;

import java.util.ArrayList;
import java.util.List;

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

    public InlineKeyboardMarkup getOpenDetailsKeyboard(int reminderId) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();
        keyboard.add(List.of(buttonFactory.reminderDetails(reminderId)));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getPostponeMessagesKeyboard(String prevCommand, RequestParams requestParams) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();

        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_REASON_MEETING), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_REASON.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_REASON_MEETING))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_WITHOUT_REASON), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_REASON.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_WITHOUT_REASON)))
        ));

        keyboard.add(List.of(buttonFactory.goBackCallbackButton(prevCommand, requestParams)));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getPostponeKeyboard(boolean hasTime, String prevCommand, RequestParams requestParams) {
        if (hasTime) {
            return getPostponeKeyboardForHasTime(prevCommand, requestParams);
        }

        return getPostponeKeyboardForWithoutTime(prevCommand, requestParams);
    }

    public InlineKeyboardMarkup getCustomRemindKeyboard(String prevCommand, RequestParams requestParams) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_10_MIN), CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME, new RequestParams().add(Arg.CUSTOM_REMIND_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_10_MIN))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_1_H), CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME, new RequestParams().add(Arg.CUSTOM_REMIND_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_1_H))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_2_H), CommandNames.CUSTOM_REMINDER_TIME_COMMAND_NAME, new RequestParams().add(Arg.CUSTOM_REMIND_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_AFTER_2_H)))
        ));
        keyboard.add(List.of(buttonFactory.goBackCallbackButton(prevCommand, requestParams)));

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

    public InlineKeyboardMarkup getPaymentKeyboard(int userId, int planId) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();
        keyboard.add(List.of(buttonFactory.paymentButton(localisationService.getMessage(MessagesProperties.PAYMENT_BEELINE_COMMAND_DESCRIPTION), buildPayUrl(userId, planId, PaymentType.BEELINE))));
        keyboard.add(List.of(buttonFactory.paymentButton(localisationService.getMessage(MessagesProperties.PAYMENT_CARD_COMMAND_DESCRIPTION), buildPayUrl(userId, planId, PaymentType.CARD))));
        keyboard.add(List.of(buttonFactory.paymentButton(localisationService.getMessage(MessagesProperties.PAYMENT_WEB_MONEY_DESCRIPTION), buildPayUrl(userId, planId, PaymentType.WEB_MONEY))));

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

    public InlineKeyboardMarkup getReminderTimeKeyboard(int reminderTimeId, int reminderId) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderTimeButton(reminderTimeId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.SCHEDULE_COMMAND_NAME, new RequestParams().add(Arg.REMINDER_ID.getKey(), reminderId))));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getReminderTimesListKeyboard(List<Integer> reminderTimesIds, int reminderId) {
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
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.suppressNotifications(reminderId)));
        }
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(MessagesProperties.CREATE_REMIND_TIME_COMMAND_DESCRIPTION, reminderId, CommandNames.SCHEDULE_COMMAND_NAME)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.REMINDER_DETAILS_COMMAND_NAME, new RequestParams().add(Arg.REMINDER_ID.getKey(), reminderId))));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendsListKeyboard(List<Integer> friendsUserIds, String commandName) {
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
                                .serialize(CommandParser.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getActiveRemindersListKeyboard(List<Integer> reminderIds, String prevHistoryName) {
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
                                .serialize(CommandParser.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(MessagesProperties.TODAY_ACTIVE_REMINDERS_COMMAND_DESCRIPTION, ReminderService.Filter.TODAY)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(MessagesProperties.ALL_ACTIVE_REMINDERS_COMMAND_DESCRIPTION, ReminderService.Filter.ALL)));
        if (prevHistoryName != null) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getCompletedRemindersListKeyboard(String prevHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteCompletedRemindersButton()));
        if (prevHistoryName != null) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getEmptyRemindersListKeyboard(String prevHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        if (prevHistoryName != null) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getRemindKeyboard(Reminder reminder) {
        InlineKeyboardMarkup inlineKeyboardMarkup = getReceiverReminderKeyboard(reminder);

        if (!reminder.getRemindAt().hasTime()) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.okButton(reminder.getId())));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getCreatorReminderKeyboard(Reminder reminder) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.reminderDetails(reminder.getId())));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getReceiverReminderKeyboard(Reminder reminder) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        if (reminder.isRepeatable()) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeRepeatReminderButton(reminder.getId()), buttonFactory.cancelReminderButton(reminder.getId())));
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.skipRepeatReminderButton(reminder.getId()), buttonFactory.stopRepeatReminderButton(reminder.getId())));
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME)));
        } else {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeReminderButton(reminder.getId()), buttonFactory.cancelReminderButton(reminder.getId())));
            List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
            keyboardButtons.add(buttonFactory.customReminderTimeButton(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME));
            keyboardButtons.add(buttonFactory.postponeReminderButton(reminder.getId()));

            inlineKeyboardMarkup.getKeyboard().add(keyboardButtons);
        }
        if (!reminder.isSuppressNotifications()) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.suppressNotifications(reminder.getId())));
        }
        if (reminder.isNotMySelf() && reminder.isUnread()) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.readButton(reminder.getId())));
        }
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.reminderDetails(reminder.getId())));

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getReceiverReminderDetailsKeyboard(Reminder reminder) {
        if (reminder.isInactive()) {
            return null;
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = getInitialReceiverReminderDetailsKeyboard(reminder);

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getRemindersMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getCompletedRemindersButton()));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_DESCRIPTION, ReminderService.Filter.ALL)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendKeyboard(int friendUserId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.createFriendReminderButton(friendUserId),
                buttonFactory.deleteFriendButton(friendUserId))
        );
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.changeFriendNameButton(friendUserId)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.goBackCallbackButton(CommandNames.GET_FRIENDS_COMMAND_HISTORY_NAME)
        ));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendRequestKeyboard(int friendUserId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.acceptFriendRequestButton(friendUserId),
                buttonFactory.rejectFriendRequestButton(friendUserId)
        ));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(MessagesProperties.TO_ME_FRIEND_REQUESTS_COMMAND_NAME)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup goBackCallbackButton(String prevHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup goBackCallbackButton(String prevHistoryName, CallbackCommandNavigator.RestoreKeyboard restoreKeyboard, RequestParams requestParams) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, requestParams.add(Arg.RESTORE_KEYBOARD.getKey(), restoreKeyboard.getCode()))));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup goBackCallbackButton(String prevHistoryName, RequestParams requestParams) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, requestParams)));

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getCreatorReminderDetailsKeyboard(Reminder reminder) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();
        addControlReminderButtons(keyboardMarkup, reminder);

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME)));

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getMySelfReminderDetailsKeyboard(Reminder reminder) {
        InlineKeyboardMarkup keyboardMarkup = getInitialReceiverReminderDetailsKeyboard(reminder);

        addControlReminderButtons(keyboardMarkup, reminder);

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME)));


        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getReminderDetailsKeyboard(int currUserId, Reminder reminder) {
        if (reminder.getCreatorId() == reminder.getReceiverId()) {
            return getMySelfReminderDetailsKeyboard(reminder);
        } else if (currUserId == reminder.getReceiverId()) {
            return getReceiverReminderDetailsKeyboard(reminder);
        } else {
            return getCreatorReminderDetailsKeyboard(reminder);
        }
    }

    public InlineKeyboardMarkup getEditReminderKeyboard(int reminderId, String prevHistoryName) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminderTimeButton(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminderTextButton(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.changeReminderNote(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderNote(reminderId)));

        if (prevHistoryName != null) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, new RequestParams().add(Arg.REMINDER_ID.getKey(), reminderId))));
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
        if (reminder.isRepeatable()) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeRepeatReminderButton(reminderId), buttonFactory.cancelReminderButton(reminderId)));
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.skipRepeatReminderButton(reminderId), buttonFactory.stopRepeatReminderButton(reminderId)));
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.returnReminderButton(reminderId), buttonFactory.customReminderTimeButton(reminderId, CommandNames.REMINDER_DETAILS_COMMAND_NAME)));
        } else {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeReminderButton(reminderId), buttonFactory.cancelReminderButton(reminderId)));
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(reminderId, CommandNames.REMINDER_DETAILS_COMMAND_NAME), buttonFactory.postponeReminderButton(reminderId)));
        }
        if (!reminder.isSuppressNotifications()) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.suppressNotifications(reminderId)));
        }
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.reminderTimesScheduleButton(reminderId)));
        if (reminder.isNotMySelf() && reminder.isUnread()) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.readButton(reminder.getId())));
        }

        return keyboardMarkup;
    }

    private void addControlReminderButtons(InlineKeyboardMarkup keyboardMarkup, Reminder reminder) {
        if (reminder.isInactive()) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.activateReminderButton(reminder.getId())));
        } else {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminder(reminder.getId())));
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deactivateReminderButton(reminder.getId())));
            if (reminder.isRepeatable()) {
                if (reminder.isCountSeries()) {
                    keyboardMarkup.getKeyboard().add(List.of(buttonFactory.disableCountSeries(reminder.getId())));
                } else {
                    keyboardMarkup.getKeyboard().add(List.of(buttonFactory.enableCountSeries(reminder.getId())));
                }
            }
        }
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderButton(reminder.getId())));
    }

    private InlineKeyboardMarkup getPostponeKeyboardForWithoutTime(String prevCommand, RequestParams requestParams) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_D), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_D))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_2_D), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_2_D)))
        ));
        keyboard.add(List.of(buttonFactory.goBackCallbackButton(prevCommand, requestParams)));

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getPostponeKeyboardForHasTime(String prevCommand, RequestParams requestParams) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_15_MIN), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_15_MIN))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_30_MIN), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_30_MIN))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_H), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_H)))
        ));
        keyboard.add(List.of(
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_2_H), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_2_H))),
                buttonFactory.delegateButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_D), CommandNames.POSTPONE_REMINDER_COMMAND_NAME, new RequestParams().add(Arg.POSTPONE_TIME.getKey(), localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_1_D)))
        ));
        keyboard.add(List.of(buttonFactory.goBackCallbackButton(prevCommand, requestParams)));

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
