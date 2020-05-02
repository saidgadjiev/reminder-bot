package ru.gadjini.reminder.domain.jooq.datatype;

import org.jooq.UDTField;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.UDTImpl;
import ru.gadjini.reminder.domain.time.RepeatTime;

public class RepeatTimeType extends UDTImpl<RepeatTimeRecord> {

    public static final RepeatTimeType REPEAT_TIME_TYPE = new RepeatTimeType();

    public static UDTField<RepeatTimeRecord, String> RT_DAY_OF_WEEK;

    public static UDTField<RepeatTimeRecord, String> RT_TIME;

    public static UDTField<RepeatTimeRecord, String> RT_INTERVAL;

    public static UDTField<RepeatTimeRecord, String> RT_MONTH;

    public static UDTField<RepeatTimeRecord, Integer> RT_DAY;

    public static UDTField<RepeatTimeRecord, Integer> RT_SERIES_TO_COMPLETE;

    public RepeatTimeType() {
        super(RepeatTime.TYPE, null);
        createFields();
    }

    @Override
    public Class<RepeatTimeRecord> getRecordType() {
        return RepeatTimeRecord.class;
    }

    private void createFields() {
        RT_DAY_OF_WEEK = createField(RepeatTime.WEEK_DAY, SQLDataType.VARCHAR, this);
        RT_TIME = createField(RepeatTime.TIME, SQLDataType.VARCHAR, this);
        RT_INTERVAL = createField(RepeatTime.INTERVAL, SQLDataType.VARCHAR, this);
        RT_MONTH = createField(RepeatTime.MONTH, SQLDataType.VARCHAR, this);
        RT_DAY = createField(RepeatTime.DAY, SQLDataType.INTEGER, this);
        RT_SERIES_TO_COMPLETE = createField(RepeatTime.SERIES_TO_COMPLETE, SQLDataType.INTEGER, this);
    }
}
