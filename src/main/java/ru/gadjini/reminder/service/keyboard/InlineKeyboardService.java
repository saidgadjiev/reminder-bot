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
import ru.gadjini.reminder.domain.*;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandParser;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.tag.ReminderTagService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class InlineKeyboardService {

    private LocalisationService localisationService;

    private ButtonFactory buttonFactory;

    private static final String PAYMENT_API_PATH = "payment/pay";

    @Autowired
    public InlineKeyboardService(LocalisationService localisationService, ButtonFactory buttonFactory) {
        this.localisationService = localisationService;
        this.buttonFactory = buttonFactory;
    }

    public InlineKeyboardMarkup goalDetails(int goalId, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.createGoalButton(goalId, locale)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteGoalButton(goalId, locale),
                buttonFactory.completeGoalButton(goalId, locale)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.getGoalsButton(goalId, locale),
                buttonFactory.goBackCallbackButton(CommandNames.GET_GOALS_COMMAND_NAME, locale)));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup reminderTagsKeyboard(List<Tag> tags, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<Tag>> partition = Lists.partition(tags, 2);
        for (List<Tag> tagList : partition) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (Tag tag : tagList) {
                buttons.add(buttonFactory.getActiveRemindersButton(tag.getText(), tag.getId(), ReminderDao.Filter.TODAY));
            }
            keyboardMarkup.getKeyboard().add(buttons);
        }
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(
                localisationService.getMessage(MessagesProperties.MESSAGE_NO_TAG, locale),
                ReminderTagService.NO_TAG_ID,
                ReminderDao.Filter.TODAY
        )));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME, locale)));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getTagsKeyboard(int reminderId, List<Tag> tags, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<List<Tag>> partition = Lists.partition(tags, 2);
        for (List<Tag> tagList : partition) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (Tag tag : tagList) {
                buttons.add(buttonFactory.tagButton(reminderId, tag.getId(), tag.getText()));
            }
            keyboardMarkup.getKeyboard().add(buttons);
        }
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.TAGS_COMMAND_NAME, locale)));

        return keyboardMarkup;
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

    public InlineKeyboardMarkup goalsKeyboard(Integer goalId, List<Goal> goals, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        int i = 1;
        List<List<Goal>> lists = Lists.partition(goals, 4);
        for (List<Goal> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (Goal goal : list) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i++));
                button.setCallbackData(CommandNames.GOAL_DETAILS_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                        new RequestParams().add(Arg.GOAL_ID.getKey(), goal.getId()).serialize(CommandParser.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(buttonFactory.createGoalButton(null, locale));
        if (goalId != null) {
            buttons.add(buttonFactory.goBackCallbackButton(CommandNames.GET_GOALS_COMMAND_NAME, locale));
        } else {
            buttons.add(buttonFactory.allSubGoalsButton(locale));
        }
        inlineKeyboardMarkup.getKeyboard().add(buttons);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup goalsKeyboard(List<Goal> goals, Locale locale) {
        return goalsKeyboard(null, goals, locale);
    }

    public InlineKeyboardMarkup getPaymentKeyboard(long userId, int planId, Locale locale) {
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

    public InlineKeyboardMarkup getChallengeCreatedKeyboard(int challengeId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.openChallengeDetails(challengeId, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getChooseChallengeParticipantKeyboard(List<Long> friendUserIds, Locale locale) {
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
        friendsListKeyboard.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(CommandNames.START_COMMAND_NAME, locale)));

        return friendsListKeyboard;
    }

    public InlineKeyboardMarkup getFriendsListKeyboard(List<Long> friendsUserIds, String commandName, RequestParams requestParams) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        int i = 1;
        List<List<Long>> lists = Lists.partition(friendsUserIds, 4);
        for (List<Long> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (long friendUserId : list) {
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

    public InlineKeyboardMarkup getUserChallengesKeyboard(List<Integer> challengesIds) {
        if (challengesIds.isEmpty()) {
            return null;
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        int i = 1;
        List<List<Integer>> lists = Lists.partition(challengesIds, 4);
        for (List<Integer> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int challengeId : list) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i++));
                button.setCallbackData(CommandNames.CHALLENGE_DETAILS_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                        new RequestParams()
                                .add(Arg.CHALLENGE_ID.getKey(), challengeId)
                                .serialize(CommandParser.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getActiveRemindersListKeyboard(List<Integer> reminderIds,
                                                               String prevHistoryName, RequestParams requestParams,
                                                               Locale locale) {
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
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(
                MessagesProperties.TODAY_ACTIVE_REMINDERS_COMMAND_DESCRIPTION,
                requestParams.getInt(Arg.TAG_ID.getKey()), ReminderDao.Filter.TODAY, locale),
                buttonFactory.getActiveRemindersButton(MessagesProperties.ALL_ACTIVE_REMINDERS_COMMAND_DESCRIPTION,
                        requestParams.getInt(Arg.TAG_ID.getKey()),
                        ReminderDao.Filter.ALL, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(
                MessagesProperties.EXPIRED_REMINDERS_COMMAND_DESCRIPTION, requestParams.getInt(Arg.TAG_ID.getKey()),
                ReminderDao.Filter.EXPIRED, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteAll(requestParams, locale),
                buttonFactory.goBackCallbackButton(prevHistoryName, locale)));

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

    public InlineKeyboardMarkup getEmptyActiveRemindersListKeyboard(String prevHistoryName, int tagId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(
                MessagesProperties.TODAY_ACTIVE_REMINDERS_COMMAND_DESCRIPTION, tagId, ReminderDao.Filter.TODAY, locale),
                buttonFactory.getActiveRemindersButton(MessagesProperties.ALL_ACTIVE_REMINDERS_COMMAND_DESCRIPTION, tagId,
                        ReminderDao.Filter.ALL, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton(
                MessagesProperties.EXPIRED_REMINDERS_COMMAND_DESCRIPTION, tagId,
                ReminderDao.Filter.EXPIRED, locale)));
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

        if (reminder.getChallengeId() != null) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeRepeatReminderButton(reminder.getId(), reminder.getReceiver().getLocale()), buttonFactory.customReminderTimeButton(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME, reminder.getReceiver().getLocale())));
        } else if (reminder.isRepeatableWithoutTime()) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeRepeatReminderButton(reminder.getId(), reminder.getReceiver().getLocale()), buttonFactory.cancelReminderButton(reminder.getId(), reminder.getReceiver().getLocale())));
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME, reminder.getReceiver().getLocale()), buttonFactory.stopRepeatReminderButton(reminder.getId(), reminder.getReceiver().getLocale())));
        } else if (reminder.isRepeatableWithTime()) {
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
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.addTagButton(reminder.getId(), reminder.getReceiver().getLocale())));
        addTimeTrackerButtons(inlineKeyboardMarkup, reminder);
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
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<InlineKeyboardButton> buttons = addInitialReceiverReminderDetailsKeyboard(reminder);

        addButtons(keyboardMarkup, buttons);
        keyboardMarkup.getKeyboard().add(List.of(backToActiveRemindersList(requestParams, reminder.getCreator().getLocale())));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getRemindersMenu(Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getCompletedRemindersButton(locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.geReminderTagsButton(locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendKeyboard(long friendUserId, Locale locale) {
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

    public InlineKeyboardMarkup getChallengeDetailsKeyboard(ChallengeParticipant requester, long challengeCreatorId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();
        Locale locale = requester.getUser().getLocale();
        if (requester.getState() == ChallengeParticipant.State.ACCEPTED) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.openReminderDetailsFromChallenge(requester.getReminderId(), locale)));
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.giveUp(requester.getChallengeId(), locale)));
            if (challengeCreatorId != requester.getUserId()) {
                inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.giveUpAndExit(requester.getChallengeId(), locale)));
            }
        } else if (requester.getState() == ChallengeParticipant.State.GAVE_UP && requester.getUserId() != challengeCreatorId) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.exit(requester.getChallengeId(), locale)));
        }
        if (challengeCreatorId == requester.getUserId()) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteChallenge(requester.getChallengeId(), locale)));
        }
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(MessagesProperties.BACK_TO_CHALLENGES_LIST_DESCRIPTION, CommandNames.GET_CHALLENGES_COMMAND_NAME, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendRequestKeyboard(long friendUserId, Locale locale) {
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
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        addControlReminderButtons(buttons, requestParams, reminder);

        addButtons(keyboardMarkup, buttons);
        keyboardMarkup.getKeyboard().add(List.of(backToActiveRemindersList(requestParams, reminder.getCreator().getLocale())));

        return keyboardMarkup;
    }

    private InlineKeyboardButton backToActiveRemindersList(RequestParams requestParams, Locale locale) {
        if (requestParams.contains(Arg.TAG_ID.getKey())) {
            return buttonFactory.goBackCallbackButton(MessagesProperties.BACK_TO_REMINDERS_LIST_COMMAND_DESCRIPTION,
                    CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME, requestParams, locale);
        } else {
            return buttonFactory.goBackCallbackButton(MessagesProperties.BACK_TO_REMINDERS_LIST_COMMAND_DESCRIPTION,
                    CommandNames.TAGS_COMMAND_NAME, requestParams, locale);
        }
    }

    private InlineKeyboardMarkup getChallengeReminderDetailsKeyboard(Reminder reminder, RequestParams requestParams) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();
        Locale locale = reminder.getReceiver().getLocale();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeRepeatReminderButton(reminder.getId(), locale), buttonFactory.customReminderTimeButton(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME, locale)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.returnReminderButton(reminder.getId(), locale), buttonFactory.reminderTimesScheduleButton(reminder.getId(), locale)));
        if (!reminder.isSuppressNotifications()) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.suppressNotifications(reminder.getId(), locale)));
        }
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminder(reminder.getId(), locale, new RequestParams().add(Arg.CHALLENGE_ID.getKey(), reminder.getChallengeId()))));
        if (reminder.isCountSeries()) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.disableCountSeries(reminder.getId(), locale)));
        } else {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.enableCountSeries(reminder.getId(), locale)));
        }
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.openChallengeDetailsFromReminder(reminder.getChallengeId(), locale)));

        keyboardMarkup.getKeyboard().add(List.of(backToActiveRemindersList(requestParams, reminder.getCreator().getLocale())));

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getMySelfReminderDetailsKeyboard(Reminder reminder, RequestParams requestParams) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        List<InlineKeyboardButton> buttons = addInitialReceiverReminderDetailsKeyboard(reminder);

        addControlReminderButtons(buttons, requestParams, reminder);

        addButtons(keyboardMarkup, buttons);
        keyboardMarkup.getKeyboard().add(List.of(backToActiveRemindersList(requestParams, reminder.getCreator().getLocale())));

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getReminderDetailsKeyboard(long currUserId, RequestParams requestParams, Reminder reminder) {
        if (reminder.getChallengeId() != null) {
            return getChallengeReminderDetailsKeyboard(reminder, requestParams);
        } else if (Objects.equals(reminder.getCreatorId(), reminder.getReceiverId())) {
            return getMySelfReminderDetailsKeyboard(reminder, requestParams);
        } else if (currUserId == reminder.getReceiverId()) {
            return getReceiverReminderDetailsKeyboard(reminder, requestParams);
        } else {
            return getCreatorReminderDetailsKeyboard(reminder, requestParams);
        }
    }

    public InlineKeyboardMarkup getEditReminderKeyboard(int reminderId, Integer challengeId, String prevHistoryName, Locale locale) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        if (challengeId == null) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminderTimeButton(reminderId, locale)));
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminderTextButton(reminderId, locale)));
        }
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.changeReminderNote(reminderId, locale)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderNote(reminderId, locale)));

        if (prevHistoryName != null) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, new RequestParams().add(Arg.REMINDER_ID.getKey(), reminderId), locale)));
        }

        return keyboardMarkup;
    }

    private void addButtons(InlineKeyboardMarkup inlineKeyboardMarkup, List<InlineKeyboardButton> buttons) {
        List<List<InlineKeyboardButton>> partition = Lists.partition(buttons, 2);

        for (List<InlineKeyboardButton> list : partition) {
            inlineKeyboardMarkup.getKeyboard().add(list);
        }
    }

    private InlineKeyboardMarkup inlineKeyboardMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.setKeyboard(new ArrayList<>());

        return inlineKeyboardMarkup;
    }

    private List<InlineKeyboardButton> addInitialReceiverReminderDetailsKeyboard(Reminder reminder) {
        if (reminder.isInactive()) {
            return List.of();
        }

        int reminderId = reminder.getId();
        Locale locale = reminder.getReceiver().getLocale();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        if (reminder.isRepeatableWithTime() || reminder.isRepeatableWithoutTime()) {
            buttons.add(buttonFactory.completeRepeatReminderButton(reminderId, locale));
        } else {
            buttons.add(buttonFactory.completeReminderButton(reminderId, locale));
        }
        buttons.add(buttonFactory.cancelReminderButton(reminderId, locale));

        if (reminder.isRepeatableWithoutTime()) {
            buttons.add(buttonFactory.customReminderTimeButton(reminderId, CommandNames.REMINDER_DETAILS_COMMAND_NAME, locale));
            buttons.add(buttonFactory.stopRepeatReminderButton(reminderId, locale));
            buttons.add(buttonFactory.returnReminderButton(reminderId, locale));
        } else if (reminder.isRepeatableWithTime()) {
            buttons.add(buttonFactory.skipRepeatReminderButton(reminderId, locale));
            buttons.add(buttonFactory.stopRepeatReminderButton(reminderId, locale));
            buttons.add(buttonFactory.returnReminderButton(reminderId, locale));
            buttons.add(buttonFactory.customReminderTimeButton(reminderId, CommandNames.REMINDER_DETAILS_COMMAND_NAME, locale));
        } else {
            buttons.add(buttonFactory.customReminderTimeButton(reminderId, CommandNames.REMINDER_DETAILS_COMMAND_NAME, locale));
            buttons.add(buttonFactory.postponeReminderButton(reminderId, locale));
        }
        buttons.add(buttonFactory.addTagButton(reminderId, locale));
        addTimeTrackerButtons(buttons, reminder);
        if (!reminder.isSuppressNotifications()) {
            buttons.add(buttonFactory.suppressNotifications(reminderId, locale));
        }
        buttons.add(buttonFactory.reminderTimesScheduleButton(reminderId, locale));
        if (reminder.isNotMySelf() && reminder.isUnread()) {
            buttons.add(buttonFactory.readButton(reminder.getId(), locale));
        }

        return buttons;
    }

    private void addControlReminderButtons(List<InlineKeyboardButton> buttons, RequestParams requestParams, Reminder reminder) {
        Locale locale = reminder.getCreator().getLocale();
        if (reminder.isInactive()) {
            buttons.add(buttonFactory.activateReminderButton(reminder.getId(), requestParams, locale));
        } else {
            buttons.add(buttonFactory.editReminder(reminder.getId(), locale));
            buttons.add(buttonFactory.deactivateReminderButton(reminder.getId(), requestParams, locale));
            if (reminder.isRepeatableWithTime()) {
                if (reminder.isCountSeries()) {
                    buttons.add(buttonFactory.disableCountSeries(reminder.getId(), locale));
                } else {
                    buttons.add(buttonFactory.enableCountSeries(reminder.getId(), locale));
                }
            }
        }
        buttons.add(buttonFactory.deleteReminderButton(reminder.getId(), locale));
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

    private void addTimeTrackerButtons(InlineKeyboardMarkup keyboardMarkup, Reminder reminder) {
        if (reminder.isTimeTracker()) {
            if (reminder.getStatus() == Reminder.Status.IN_PROGRESS) {
                keyboardMarkup.getKeyboard().add(List.of(buttonFactory.stopReminder(reminder.getId(), reminder.getReceiver().getLocale())));
            } else {
                keyboardMarkup.getKeyboard().add(List.of(buttonFactory.startReminder(reminder.getId(), reminder.getReceiver().getLocale())));
            }
        }
    }

    private void addTimeTrackerButtons(List<InlineKeyboardButton> buttons, Reminder reminder) {
        if (reminder.isTimeTracker()) {
            if (reminder.getStatus() == Reminder.Status.IN_PROGRESS) {
                buttons.add(buttonFactory.stopReminder(reminder.getId(), reminder.getReceiver().getLocale()));
            } else {
                buttons.add(buttonFactory.startReminder(reminder.getId(), reminder.getReceiver().getLocale()));
            }
        }
    }

    private String buildPayUrl(long userId, int planId, PaymentType paymentType) {
        return UriComponentsBuilder.fromHttpUrl("")
                .path(PAYMENT_API_PATH)
                .queryParam("planId", planId)
                .queryParam("userId", userId)
                .queryParam("paymentType", paymentType.getType())
                .toUriString();
    }
}
