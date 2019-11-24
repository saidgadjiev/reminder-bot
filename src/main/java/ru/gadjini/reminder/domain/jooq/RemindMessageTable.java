package ru.gadjini.reminder.domain.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import ru.gadjini.reminder.domain.RemindMessage;

public class RemindMessageTable extends TableImpl<Record> {

    public static final RemindMessageTable TABLE = new RemindMessageTable();

    public TableField<Record, Integer> REMINDER_ID;

    public TableField<Record, Integer> MESSAGE_ID;

    private RemindMessageTable() {
        this(DSL.name(RemindMessage.TYPE), null, null);
    }

    private RemindMessageTable(Name alias, Table<Record> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
        createFields();
    }

    private RemindMessageTable(Name alias, Table<Record> aliased) {
        this(alias, aliased, null);
    }

    private void createFields() {
        createField(RemindMessage.ID, SQLDataType.INTEGER);
        REMINDER_ID = createField(RemindMessage.REMINDER_ID, SQLDataType.INTEGER);
        MESSAGE_ID = createField(RemindMessage.MESSAGE_ID, SQLDataType.INTEGER);
    }

    @Override
    public RemindMessageTable as(String alias) {
        return new RemindMessageTable(DSL.name(alias), this);
    }

}
