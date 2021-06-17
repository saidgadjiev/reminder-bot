package ru.gadjini.reminder.domain.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.jooq.datatype.RepeatTimeRecord;
import ru.gadjini.reminder.domain.jooq.datatype.RepeatTimeType;
import ru.gadjini.reminder.domain.time.RepeatTime;

public class ReminderTable extends TableImpl<Record> {

    public static final ReminderTable TABLE = new ReminderTable();

    public TableField<Record, Integer> ID;

    public TableField<Record, Long> CREATOR_ID;

    public TableField<Record, Long> RECEIVER_ID;

    public TableField<Record, Object> INITIAL_REMIND_AT;

    public TableField<Record, RepeatTimeRecord[]> REPEAT_REMIND_AT;

    public TableField<Record, Object> REMIND_AT;

    public TableField<Record, String> TEXT;

    public TableField<Record, String> NOTE;

    public TableField<Record, Integer> STATUS;

    public TableField<Record, Integer> MESSAGE_ID;

    public TableField<Record, Integer> CURRENT_SERIES;

    public TableField<Record, Integer> TOTAL_SERIES;

    public TableField<Record, Integer> MAX_SERIES;

    public TableField<Record, Boolean> COUNT_SERIES;

    public TableField<Record, Boolean> READ;

    public TableField<Record, Integer> RECEIVER_MESSAGE_ID;

    public TableField<Record, Integer> CREATOR_MESSAGE_ID;

    public TableField<Record, Integer> CURR_REPEAT_INDEX;

    public TableField<Record, Integer> CHALLENGE_ID;

    public TableField<Record, Integer> CURR_SERIES_TO_COMPLETE;

    public TableField<Record, Boolean> TIME_TRACKER;

    public TableField<Record, Object> LAST_WORK_IN_PROGRESS_AT;

    public TableField<Record, Object> ESTIMATE;

    public TableField<Record, Object> ELAPSED_TIME;

    private ReminderTable() {
        this(DSL.name(Reminder.TYPE), null, null);
    }

    private ReminderTable(Name alias, Table<Record> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
        createFields();
    }

    private ReminderTable(Name alias, Table<Record> aliased) {
        this(alias, aliased, null);
    }

    private void createFields() {
        ID = createField(Reminder.ID, SQLDataType.INTEGER);
        TEXT = createField(Reminder.TEXT, SQLDataType.VARCHAR);
        CREATOR_ID = createField(Reminder.CREATOR_ID, SQLDataType.BIGINT);
        RECEIVER_ID = createField(Reminder.RECEIVER_ID, SQLDataType.BIGINT);
        TEXT = createField(Reminder.TEXT, SQLDataType.VARCHAR);
        INITIAL_REMIND_AT = createField(Reminder.INITIAL_REMIND_AT, SQLDataType.OTHER);
        REMIND_AT = createField(Reminder.REMIND_AT, SQLDataType.OTHER);
        NOTE = createField(Reminder.NOTE, SQLDataType.VARCHAR);
        STATUS = createField(Reminder.STATUS, SQLDataType.INTEGER);
        REPEAT_REMIND_AT = createField(Reminder.REPEAT_REMIND_AT, RepeatTimeType.REPEAT_TIME_TYPE.getDataType().getArrayDataType(), RepeatTime.TYPE);
        MESSAGE_ID = createField(Reminder.MESSAGE_ID, SQLDataType.INTEGER);
        CURRENT_SERIES = createField(Reminder.CURRENT_SERIES, SQLDataType.INTEGER);
        MAX_SERIES = createField(Reminder.MAX_SERIES, SQLDataType.INTEGER);
        COUNT_SERIES = createField(Reminder.COUNT_SERIES, SQLDataType.BOOLEAN);
        READ = createField(Reminder.READ, SQLDataType.BOOLEAN);
        CREATOR_MESSAGE_ID = createField(Reminder.CREATOR_MESSAGE_ID, SQLDataType.INTEGER);
        RECEIVER_MESSAGE_ID = createField(Reminder.RECEIVER_MESSAGE_ID, SQLDataType.INTEGER);
        TOTAL_SERIES = createField(Reminder.TOTAL_SERIES, SQLDataType.INTEGER);
        CURR_REPEAT_INDEX = createField(Reminder.CURR_REPEAT_INDEX, SQLDataType.INTEGER);
        CHALLENGE_ID = createField(Reminder.CHALLENGE_ID, SQLDataType.INTEGER);
        CURR_SERIES_TO_COMPLETE = createField(Reminder.CURR_SERIES_TO_COMPLETE, SQLDataType.INTEGER);
        TIME_TRACKER = createField(Reminder.TIME_TRACKER, SQLDataType.BOOLEAN);
        LAST_WORK_IN_PROGRESS_AT = createField(Reminder.LAST_WORK_IN_PROGRESS_AT, SQLDataType.OTHER);
        ESTIMATE = createField(Reminder.ESTIMATE, SQLDataType.OTHER);
        ELAPSED_TIME = createField(Reminder.ELAPSED_TIME, SQLDataType.OTHER);
    }

    @Override
    public ReminderTable as(String alias) {
        return new ReminderTable(DSL.name(alias), this);
    }
}
