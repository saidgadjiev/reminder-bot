package ru.gadjini.reminder.domain.jooq.datatype;

import org.jooq.impl.UDTRecordImpl;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.util.JodaTimeUtils;

import static ru.gadjini.reminder.domain.time.RepeatTime.DATE_TIME_FORMATTER;

public class RepeatTimeRecord extends UDTRecordImpl<RepeatTimeRecord> {

    public RepeatTimeRecord() {
        super(RepeatTimeType.REPEAT_TIME_TYPE);
    }

    public RepeatTimeRecord(RepeatTime repeatTime) {
        super(RepeatTimeType.REPEAT_TIME_TYPE);
        set(repeatTime);
    }

    private void set(RepeatTime repeatTime) {
        if (repeatTime.getDayOfWeek() == null) {
            set(0, null);
        } else {
            set(0, repeatTime.getDayOfWeek().name());
        }
        if (repeatTime.getTime() == null) {
            set(1, null);
        } else {
            set(1, repeatTime.getTime().format(DATE_TIME_FORMATTER));
        }
        if (repeatTime.getInterval() != null) {
            set(2, JodaTimeUtils.toSqlInterval(repeatTime.getInterval()));
        } else {
            set(2, null);
        }
        if (repeatTime.getMonth() == null) {
            set(3, null);
        } else {
            set(3, repeatTime.getMonth().name());
        }
        if (repeatTime.getDay() != 0) {
            set(4, repeatTime.getDay());
        } else {
            set(4, null);
        }
    }
}
