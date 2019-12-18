package ru.gadjini.reminder.domain.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;

public class ReminderNotificationTable extends TableImpl<Record> {

    public static final ReminderNotificationTable TABLE = new ReminderNotificationTable();

    public TableField<Record, Integer> ID;

    public TableField<Record, Integer> REMINDER_ID;

    public TableField<Record, Boolean> CUSTOM;

    private ReminderNotificationTable() {
        this(DSL.name(ReminderNotification.TYPE), null, null);
    }

    private ReminderNotificationTable(Name alias, Table<Record> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
        createFields();
    }

    private ReminderNotificationTable(Name alias, Table<Record> aliased) {
        this(alias, aliased, null);
    }

    private void createFields() {
        ID = createField(Reminder.ID, SQLDataType.INTEGER);
        CUSTOM = createField(ReminderNotification.CUSTOM, SQLDataType.BOOLEAN);
        REMINDER_ID = createField(ReminderNotification.REMINDER_ID, SQLDataType.INTEGER);
    }

    @Override
    public ReminderNotificationTable as(String alias) {
        return new ReminderNotificationTable(DSL.name(alias), this);
    }
}
