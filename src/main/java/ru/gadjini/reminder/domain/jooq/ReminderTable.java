package ru.gadjini.reminder.domain.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import ru.gadjini.reminder.domain.Reminder;

public class ReminderTable extends TableImpl<Record> {

    public static final ReminderTable TABLE = new ReminderTable();

    public TableField<Record, Integer> ID;

    public TableField<Record, Integer> CREATOR_ID;

    public TableField<Record, Integer> RECEIVER_ID;

    public TableField<Record, Object> INITIAL_REMIND_AT;

    public TableField<Record, Object> REPEAT_REMIND_AT;

    public TableField<Record, Object> REMIND_AT;

    public TableField<Record, String> TEXT;

    public TableField<Record, String> NOTE;

    public TableField<Record, Integer> STATUS;

    public TableField<Record, Integer> MESSAGE_ID;

    public TableField<Record, Integer> CURRENT_SERIES;

    public TableField<Record, Integer> MAX_SERIES;

    public TableField<Record, Boolean> INACTIVE;

    public TableField<Record, Boolean> COUNT_SERIES;

    public TableField<Record, Boolean> READ;

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
        CREATOR_ID = createField(Reminder.CREATOR_ID, SQLDataType.INTEGER);
        RECEIVER_ID = createField(Reminder.RECEIVER_ID, SQLDataType.INTEGER);
        TEXT = createField(Reminder.TEXT, SQLDataType.VARCHAR);
        INITIAL_REMIND_AT = createField(Reminder.INITIAL_REMIND_AT, SQLDataType.OTHER);
        REMIND_AT = createField(Reminder.REMIND_AT, SQLDataType.OTHER);
        NOTE = createField(Reminder.NOTE, SQLDataType.VARCHAR);
        STATUS = createField(Reminder.STATUS, SQLDataType.INTEGER);
        REPEAT_REMIND_AT = createField(Reminder.REPEAT_REMIND_AT, SQLDataType.OTHER);
        MESSAGE_ID = createField(Reminder.MESSAGE_ID, SQLDataType.INTEGER);
        CURRENT_SERIES = createField(Reminder.CURRENT_SERIES, SQLDataType.INTEGER);
        MAX_SERIES = createField(Reminder.MAX_SERIES, SQLDataType.INTEGER);
        INACTIVE = createField(Reminder.INACTIVE, SQLDataType.BOOLEAN);
        COUNT_SERIES = createField(Reminder.COUNT_SERIES, SQLDataType.BOOLEAN);
        READ = createField(Reminder.READ, SQLDataType.BOOLEAN);
    }

    @Override
    public ReminderTable as(String alias) {
        return new ReminderTable(DSL.name(alias), this);
    }
}
