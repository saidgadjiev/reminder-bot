package ru.gadjini.reminder.service.keyboard;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandExecutor;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyboardService {

    private LocalisationService localisationService;

    private ButtonFactory buttonFactory;

    @Autowired
    public KeyboardService(LocalisationService localisationService, ButtonFactory buttonFactory) {
        this.localisationService = localisationService;
        this.buttonFactory = buttonFactory;
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
                button.setCallbackData(MessagesProperties.DELETE_USER_REMINDER_NOTIFICATION_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                    add(Arg.USER_REMINDER_NOTIFICATION_ID.getKey(), id);
                    add(Arg.USER_REMINDER_NOTIFICATION_TYPE.getKey(), notificationType.getCode());
                }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }

        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getUserSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(
                new KeyboardRow() {{
                    add(new KeyboardButton(localisationService.getMessage(MessagesProperties.CHANGE_TIMEZONE_COMMAND_NAME)));
                }}
        );
        replyKeyboardMarkup.getKeyboard().add(
                new KeyboardRow() {{
                    add(new KeyboardButton(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_COMMAND_NAME)));
                }}
        );
        replyKeyboardMarkup.getKeyboard().add(
                new KeyboardRow() {{
                    add(new KeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME)));
                }}
        );

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getUserReminderNotificationSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_WITH_TIME_COMMAND_NAME)));
        }});
        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_WITHOUT_TIME_COMMAND_NAME)));
        }});
        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME)));
        }});

        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup getReminderTimeKeyboard(int reminderTimeId, int reminderId) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderTimeButton(reminderTimeId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(MessagesProperties.SCHEDULE_COMMAND_NAME, false, new RequestParams() {{
            add(Arg.REMINDER_ID.getKey(), reminderId);
        }})));

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
                button.setCallbackData(MessagesProperties.REMINDER_TIME_DETAILS_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                    add(Arg.REMINDER_NOTIFICATION_ID.getKey(), reminderTimeId);
                }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(localisationService.getMessage(MessagesProperties.CREATE_REMIND_TIME_COMMAND_DESCRIPTION), reminderId, MessagesProperties.SCHEDULE_COMMAND_NAME)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(MessagesProperties.REMINDER_DETAILS_COMMAND_NAME, false, new RequestParams() {{
            add(Arg.REMINDER_ID.getKey(), reminderId);
        }})));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendsListKeyboard(List<Integer> friendsUserIds) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        int i = 1;
        List<List<Integer>> lists = Lists.partition(friendsUserIds, 4);
        for (List<Integer> list : lists) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int friendUserId : list) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i++));
                button.setCallbackData(MessagesProperties.FRIEND_DETAILS_COMMAND + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                    add(Arg.FRIEND_ID.getKey(), friendUserId);
                }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }

        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getPostponeMessagesKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_REASON_MEETING)));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_WITHOUT_REASON)));
        }});

        return replyKeyboardMarkup;
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
                button.setCallbackData(MessagesProperties.REMINDER_DETAILS_COMMAND_NAME + CommandExecutor.COMMAND_NAME_SEPARATOR + new RequestParams() {{
                    add(Arg.REMINDER_ID.getKey(), remindId);
                }}.serialize(CommandExecutor.COMMAND_ARG_SEPARATOR));
                row.add(button);
            }

            inlineKeyboardMarkup.getKeyboard().add(row);
        }
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

    public InlineKeyboardMarkup getRemindKeyboard(int reminderId, boolean itsTime, boolean repeatable) {
        InlineKeyboardMarkup inlineKeyboardMarkup = getReceiverReminderKeyboard(reminderId, repeatable);

        if (!itsTime) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.okButton()));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getReceiverReminderKeyboard(int reminderId, boolean repeatable) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        if (repeatable) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeReminderButton(reminderId, MessagesProperties.RECEIVER_REMINDER_COMMAND_NAME), buttonFactory.cancelReminderButton(reminderId, MessagesProperties.RECEIVER_REMINDER_COMMAND_NAME)));
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(localisationService.getMessage(MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION), reminderId, MessagesProperties.RECEIVER_REMINDER_COMMAND_NAME)));
        } else {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeReminderButton(reminderId, MessagesProperties.RECEIVER_REMINDER_COMMAND_NAME), buttonFactory.cancelReminderButton(reminderId, MessagesProperties.RECEIVER_REMINDER_COMMAND_NAME)));
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.customReminderTimeButton(localisationService.getMessage(MessagesProperties.CUSTOM_REMINDER_TIME_COMMAND_DESCRIPTION), reminderId, MessagesProperties.RECEIVER_REMINDER_COMMAND_NAME), buttonFactory.postponeReminderButton(reminderId, MessagesProperties.RECEIVER_REMINDER_COMMAND_NAME)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getReceiverReminderDetailsKeyboard(int reminderId, String prevHistoryName, String currHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeReminderButton(reminderId, currHistoryName), buttonFactory.cancelReminderButton(reminderId, currHistoryName)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.reminderTimesScheduleButton(reminderId)));

        if (prevHistoryName != null) {
            inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getRemindersMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getCompletedRemindersButton()));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.getActiveRemindersButton()));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendKeyboard(int friendUserId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.createFriendReminderButton(friendUserId),
                buttonFactory.deleteFriendButton(friendUserId))
        );
        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.goBackCallbackButton(MessagesProperties.GET_FRIENDS_COMMAND_HISTORY_NAME)
        ));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getFriendRequestKeyboard(int friendUserId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(
                buttonFactory.acceptFriendRequestButton(friendUserId),
                buttonFactory.rejectFriendRequestButton(friendUserId)
        ));

        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GET_REMINDERS_COMMAND_NAME));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GET_FRIENDS_COMMAND_NAME));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GET_FRIEND_REQUESTS_COMMAND_NAME));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.SEND_FRIEND_REQUEST_COMMAND_NAME));
        }});

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.USER_SETTINGS_COMMAND_NAME));
        }});

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup postponeTimeKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_15_MIN)));
            add(new KeyboardButton(localisationService.getMessage(MessagesProperties.MESSAGE_POSTPONE_30_MIN)));
        }});

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardRemove replyKeyboardRemove() {
        return new ReplyKeyboardRemove();
    }

    public ReplyKeyboardMarkup goBackCommand() {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(new KeyboardRow() {{
            add(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME));
        }});

        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup goBackCallbackButton(String prevHistoryName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup goBackCallbackButton(String prevHistoryName, boolean restoreKeyboard, RequestParams requestParams) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();

        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, restoreKeyboard, requestParams)));

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getCreatorReminderDetailsKeyboard(int reminderId, String prevHistoryName) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminder(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderButton(reminderId)));

        if (prevHistoryName != null) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getMySelfReminderDetailsKeyboard(int reminderId, String prevHistoryName, String currHistoryName) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.completeReminderButton(reminderId, currHistoryName), buttonFactory.cancelReminderButton(reminderId, currHistoryName)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminder(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.reminderTimesScheduleButton(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderButton(reminderId)));

        if (prevHistoryName != null) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName)));
        }

        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getReminderDetailsKeyboard(int currUserId, Reminder reminder) {
        if (reminder.getCreatorId() == reminder.getReceiverId()) {
            return getMySelfReminderDetailsKeyboard(reminder.getId(), MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME, MessagesProperties.REMINDER_DETAILS_COMMAND_NAME);
        } else if (currUserId == reminder.getReceiverId()) {
            return getReceiverReminderDetailsKeyboard(reminder.getId(), MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME, MessagesProperties.REMINDER_DETAILS_COMMAND_NAME);
        } else {
            return getCreatorReminderDetailsKeyboard(reminder.getId(), MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME);
        }
    }

    public InlineKeyboardMarkup getEditReminderKeyboard(int reminderId, String prevHistoryName) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkup();

        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminderTimeButton(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.editReminderTextButton(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.changeReminderNote(reminderId)));
        keyboardMarkup.getKeyboard().add(List.of(buttonFactory.deleteReminderNote(reminderId)));

        if (prevHistoryName != null) {
            keyboardMarkup.getKeyboard().add(List.of(buttonFactory.goBackCallbackButton(prevHistoryName, false, new RequestParams() {{
                add(Arg.REMINDER_ID.getKey(), reminderId);
            }})));
        }

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup replyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        replyKeyboardMarkup.setKeyboard(new ArrayList<>());
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup inlineKeyboardMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.setKeyboard(new ArrayList<>());

        return inlineKeyboardMarkup;
    }
}
