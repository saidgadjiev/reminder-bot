package ru.gadjini.reminder.domain.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import ru.gadjini.reminder.domain.Reminder;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ReminderTable extends TableImpl<Record> {

    public static final ReminderTable TABLE = new ReminderTable();

    public TableField<Record, Integer> ID;

    public TableField<Record, Integer> CREATOR_ID;

    public TableField<Record, Integer> RECEIVER_ID;

    public TableField<Record, Timestamp> INITIAL_REMIND_AT;

    public TableField<Record, Timestamp> REMIND_AT;

    public TableField<Record, String> TEXT;

    public TableField<Record, String> NOTE;

    public TableField<Record, Integer> STATUS;

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
        createField(Reminder.TEXT, SQLDataType.VARCHAR);
        CREATOR_ID = createField(Reminder.CREATOR_ID, SQLDataType.INTEGER);
        RECEIVER_ID = createField(Reminder.RECEIVER_ID, SQLDataType.INTEGER);
        TEXT = createField(Reminder.TEXT, SQLDataType.VARCHAR);
        INITIAL_REMIND_AT = createField(Reminder.INITIAL_REMIND_AT, SQLDataType.TIMESTAMP);
        REMIND_AT = createField(Reminder.REMIND_AT, SQLDataType.TIMESTAMP);
        NOTE = createField(Reminder.NOTE, SQLDataType.VARCHAR);
        STATUS = createField(Reminder.STATUS, SQLDataType.INTEGER);
    }

    @Override
    public ReminderTable as(String alias) {
        return new ReminderTable(DSL.name(alias), this);
    }

    public static void main(String[] args) {
        SelectSelectStep<Record> select = DSL.using(SQLDialect.POSTGRES).select(ReminderTable.TABLE.as("r").asterisk());

        System.out.println(select.getSQL());
    }
}
