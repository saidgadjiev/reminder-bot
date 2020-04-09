package ru.gadjini.reminder.bot.command.state;

import ru.gadjini.reminder.domain.time.FixedTime;

public class FixedTimeData {

    private DateTimeData dateTime;

    private FixedTime.Type type = FixedTime.Type.AT;

    public DateTimeData getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTimeData dateTime) {
        this.dateTime = dateTime;
    }

    public FixedTime.Type getType() {
        return type;
    }

    public void setType(FixedTime.Type type) {
        this.type = type;
    }

    public static FixedTime to(FixedTimeData fixedTimeData) {
        if (fixedTimeData == null) {
            return null;
        }
        FixedTime fixedTime = new FixedTime();

        fixedTime.setDateTime(DateTimeData.to(fixedTimeData.getDateTime()));
        fixedTime.setType(fixedTimeData.getType());

        return fixedTime;
    }

    public static FixedTimeData from(FixedTime fixedTime) {
        if (fixedTime == null) {
            return null;
        }
        FixedTimeData fixedTimeData = new FixedTimeData();

        fixedTimeData.setDateTime(DateTimeData.from(fixedTime.getDateTime()));
        fixedTimeData.setType(fixedTime.getType());

        return fixedTimeData;
    }
}
