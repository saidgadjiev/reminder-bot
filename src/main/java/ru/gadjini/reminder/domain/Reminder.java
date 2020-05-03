package ru.gadjini.reminder.domain;

import org.jooq.Field;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.jooq.datatype.RepeatTimeRecord;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.DateTimeService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class Reminder {

    public static final String TYPE = "reminder";

    public static final String ID = "id";

    public static final String TEXT = "reminder_text";

    public static final String CREATOR_ID = "creator_id";

    public static final String RECEIVER_ID = "receiver_id";

    public static final String REMIND_AT = "remind_at";

    public static final String INITIAL_REMIND_AT = "initial_remind_at";

    public static final String COMPLETED_AT = "completed_at";

    public static final String STATUS = "status";

    public static final String NOTE = "note";

    public static final String REPEAT_REMIND_AT = "repeat_remind_at";

    public static final String MESSAGE_ID = "message_id";

    public static final String CURRENT_SERIES = "current_series";

    public static final String MAX_SERIES = "max_series";

    public static final String COUNT_SERIES = "count_series";

    public static final String READ = "read";

    public static final String SUPPRESS_NOTIFICATIONS = "suppress_notifications";

    public static final String CREATOR_MESSAGE_ID = "creator_message_id";

    public static final String RECEIVER_MESSAGE_ID = "receiver_message_id";

    public static final String TOTAL_SERIES = "total_series";

    public static final String CURR_REPEAT_INDEX = "curr_repeat_index";

    public static final String CREATED_AT = "created_at";

    public static final String CHALLENGE_ID = "challenge_id";

    public static final String CURR_SERIES_TO_COMPLETE = "curr_series_to_complete";

    private int id;

    private String text;

    private Integer creatorId;

    private TgUser creator;

    private Integer receiverId;

    private TgUser receiver;

    private DateTime remindAt;

    private DateTime initialRemindAt;

    private List<ReminderNotification> reminderNotifications = new ArrayList<>();

    private Status status;

    private String note;

    private List<RepeatTime> repeatRemindAts;

    private Integer currRepeatIndex;

    private ZonedDateTime completedAt;

    private int messageId;

    private int currentSeries;

    private int maxSeries;

    private int totalSeries;

    private boolean countSeries;

    private boolean read;

    private boolean suppressNotifications;

    private Integer creatorMessageId;

    private Integer receiverMessageId;

    private ZonedDateTime createdAt;

    private Integer challengeId;

    private Integer currSeriesToComplete;

    public Reminder() {
    }

    public Reminder(Reminder reminder) {
        this.id = reminder.id;
        this.text = reminder.text;
        this.creatorId = reminder.creatorId;
        this.creator = reminder.creator;
        this.receiverId = reminder.receiverId;
        this.receiver = reminder.receiver;
        this.remindAt = reminder.remindAt;
        this.initialRemindAt = reminder.initialRemindAt;
        this.status = reminder.status;
        this.note = reminder.note;
        this.repeatRemindAts = reminder.repeatRemindAts;
        this.completedAt = reminder.completedAt;
        this.messageId = reminder.messageId;
        this.reminderNotifications = reminder.reminderNotifications;
        this.currentSeries = reminder.currentSeries;
        this.maxSeries = reminder.maxSeries;
        this.countSeries = reminder.countSeries;
        this.read = reminder.read;
        this.suppressNotifications = reminder.suppressNotifications;
        this.creatorMessageId = reminder.creatorMessageId;
        this.receiverMessageId = reminder.receiverMessageId;
        this.totalSeries = reminder.totalSeries;
        this.createdAt = reminder.createdAt;
        this.currRepeatIndex = reminder.currRepeatIndex;
        this.challengeId = reminder.challengeId;
        this.currSeriesToComplete = reminder.currSeriesToComplete;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DateTime getRemindAt() {
        return remindAt;
    }

    public DateTime getRemindAtInReceiverZone() {
        return remindAt != null ? remindAt.withZoneSameInstant(receiver.getZone()) : null;
    }

    public void setRemindAt(DateTime remindAt) {
        this.remindAt = remindAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public List<ReminderNotification> getReminderNotifications() {
        return reminderNotifications;
    }

    public void setReminderNotifications(List<ReminderNotification> reminderNotifications) {
        this.reminderNotifications = reminderNotifications;
    }

    public TgUser getReceiver() {
        return receiver;
    }

    public ZoneId getReceiverZoneId() {
        return receiver.getZone();
    }

    public void setReceiver(TgUser receiver) {
        this.receiver = receiver;
    }

    public void setCreator(TgUser tgUser) {
        this.creator = tgUser;
    }

    public TgUser getCreator() {
        return creator;
    }

    public boolean hasReceiverMessage() {
        return receiverMessageId != null;
    }

    public DateTime getInitialRemindAt() {
        return initialRemindAt;
    }

    public void setInitialRemindAt(DateTime initialRemindAt) {
        this.initialRemindAt = initialRemindAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isRepeatableWithTime() {
        return repeatRemindAts != null && !isRepeatableWithoutTime();
    }

    public RepeatTime getRepeatRemindAt() {
        return repeatRemindAts.get(currRepeatIndex);
    }

    public List<RepeatTime> getRepeatRemindAtsInReceiverZone(DateTimeService timeCreator) {
        return repeatRemindAts == null ? null : timeCreator.withZone(repeatRemindAts, receiver.getZone());
    }

    public void setRepeatRemindAts(List<RepeatTime> repeatRemindAts) {
        this.repeatRemindAts = repeatRemindAts;
    }

    public List<RepeatTime> getRepeatRemindAts() {
        return repeatRemindAts;
    }

    public ZonedDateTime getCompletedAt() {
        return completedAt;
    }

    public ZonedDateTime getCompletedAtInReceiverZone() {
        return completedAt.withZoneSameInstant(receiver.getZone());
    }

    public void setCompletedAt(ZonedDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isMySelf() {
        return Objects.equals(creatorId, receiverId);
    }

    public boolean isNotMySelf() {
        return !Objects.equals(creatorId, receiverId);
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getCurrentSeries() {
        return currentSeries;
    }

    public void setCurrentSeries(int currentSeries) {
        this.currentSeries = currentSeries;
    }

    public int getMaxSeries() {
        return maxSeries;
    }

    public void setMaxSeries(int maxSeries) {
        this.maxSeries = maxSeries;
    }

    public boolean isInactive() {
        return status == Status.INACTIVE;
    }

    public boolean isCountSeries() {
        return countSeries;
    }

    public void setCountSeries(boolean countSeries) {
        this.countSeries = countSeries;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isUnread() {
        return !isRead();
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Integer getCreatorMessageId() {
        return creatorMessageId;
    }

    public void setCreatorMessageId(Integer creatorMessageId) {
        this.creatorMessageId = creatorMessageId;
    }

    public Integer getReceiverMessageId() {
        return receiverMessageId;
    }

    public void setReceiverMessageId(Integer receiverMessageId) {
        this.receiverMessageId = receiverMessageId;
    }

    public int getTotalSeries() {
        return totalSeries;
    }

    public void setTotalSeries(int totalSeries) {
        this.totalSeries = totalSeries;
    }

    public Integer getCurrRepeatIndex() {
        return currRepeatIndex;
    }

    public void setCurrRepeatIndex(Integer currRepeatIndex) {
        this.currRepeatIndex = currRepeatIndex;
    }

    public boolean isSuppressNotifications() {
        return suppressNotifications;
    }

    public void setSuppressNotifications(boolean suppressNotifications) {
        this.suppressNotifications = suppressNotifications;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getCreatedAtInReceiverZone() {
        return createdAt.withZoneSameInstant(receiver.getZone());
    }

    public Integer getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Integer challengeId) {
        this.challengeId = challengeId;
    }

    public Integer getCurrSeriesToComplete() {
        return currSeriesToComplete;
    }

    public void setCurrSeriesToComplete(Integer currSeriesToComplete) {
        this.currSeriesToComplete = currSeriesToComplete;
    }

    public boolean isRepeatableWithoutTime() {
        return repeatRemindAts != null && getRepeatRemindAts().get(0).isEmpty();
    }

    public boolean isRepeatable() {
        return isRepeatableWithTime() || isRepeatableWithoutTime();
    }

    public boolean hasRemindAt() {
        return remindAt != null;
    }

    public Map<Field<?>, Object> getDiff(Reminder newReminder) {
        Map<Field<?>, Object> values = new HashMap<>();
        if (!Objects.equals(getText(), newReminder.getText())) {
            values.put(ReminderTable.TABLE.TEXT, newReminder.getText());
        }
        if (!Objects.equals(getNote(), newReminder.getNote())) {
            values.put(ReminderTable.TABLE.NOTE, newReminder.getNote());
        }
        if (!Objects.equals(getRepeatRemindAts(), newReminder.getRepeatRemindAts())) {
            values.put(ReminderTable.TABLE.REPEAT_REMIND_AT, newReminder.getRepeatRemindAts() == null ? null : newReminder.getRepeatRemindAts().stream().map(RepeatTimeRecord::new).toArray());
        }
        if (!Objects.equals(getRemindAt(), newReminder.getRemindAt())) {
            values.put(ReminderTable.TABLE.REMIND_AT, newReminder.getRemindAt() == null ? null : newReminder.getRemindAt().sqlObject());
        }

        return values;
    }

    public enum Status {

        ACTIVE(0),
        INACTIVE(2),
        COMPLETED(1);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status fromCode(int code) {
            for (Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }

            throw new IllegalArgumentException();
        }
    }
}
