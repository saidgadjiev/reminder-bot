package ru.gadjini.reminder.domain.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import ru.gadjini.reminder.domain.TgUser;

public class TgUserTable extends TableImpl<Record> {

    public static final TgUserTable TABLE = new TgUserTable();

    public TableField<Record, Integer> ID;

    public TableField<Record, Integer> USER_ID;

    public TableField<Record, Integer> CHAT_ID;

    public TableField<Record, String> FIRST_NAME;

    public TableField<Record, String> LAST_NAME;

    public TableField<Record, String> ZONE_ID;

    private TgUserTable() {
        this(DSL.name(TgUser.TYPE), null, null);
    }

    private TgUserTable(Name alias, Table<Record> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
        createFields();
    }

    private TgUserTable(Name alias, Table<Record> aliased) {
        this(alias, aliased, null);
    }

    private void createFields() {
        ID = createField(TgUser.ID, SQLDataType.INTEGER);
        USER_ID = createField(TgUser.USER_ID, SQLDataType.INTEGER);
        CHAT_ID = createField(TgUser.CHAT_ID, SQLDataType.INTEGER);
        FIRST_NAME = createField(TgUser.FIRST_NAME, SQLDataType.VARCHAR);
        LAST_NAME = createField(TgUser.LAST_NAME, SQLDataType.VARCHAR);
        ZONE_ID = createField(TgUser.ZONE_ID, SQLDataType.VARCHAR);
    }

    @Override
    public TgUserTable as(String alias) {
        return new TgUserTable(DSL.name(alias), this);
    }
}
