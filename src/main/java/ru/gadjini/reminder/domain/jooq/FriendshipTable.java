package ru.gadjini.reminder.domain.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import ru.gadjini.reminder.domain.Friendship;

public class FriendshipTable extends TableImpl<Record> {

    public static final FriendshipTable TABLE = new FriendshipTable();

    public TableField<Record, Long> USER_ONE_ID;

    public TableField<Record, Long> USER_TWO_ID;

    public TableField<Record, Integer> STATUS;

    private FriendshipTable() {
        this(DSL.name(Friendship.TYPE), null, null);
    }

    private FriendshipTable(Name alias, Table<Record> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
        createFields();
    }

    private FriendshipTable(Name alias, Table<Record> aliased) {
        this(alias, aliased, null);
    }

    private void createFields() {
        USER_ONE_ID = createField(Friendship.USER_ONE_ID, SQLDataType.BIGINT);
        USER_TWO_ID = createField(Friendship.USER_TWO_ID, SQLDataType.BIGINT);
        STATUS = createField(Friendship.STATUS, SQLDataType.INTEGER);
    }

    @Override
    public FriendshipTable as(String alias) {
        return new FriendshipTable(DSL.name(alias), this);
    }
}
