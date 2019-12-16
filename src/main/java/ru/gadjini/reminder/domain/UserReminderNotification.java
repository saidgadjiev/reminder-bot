package ru.gadjini.reminder.domain;

import java.time.LocalTime;

public class UserReminderNotification {

    private OffsetTime offsetTime;

    public OffsetTime getOffsetTime() {
        return offsetTime;
    }

    public void setOffsetTime(OffsetTime offsetTime) {
        this.offsetTime = offsetTime;
    }

    public enum ReminderType {
        WITHOUT_TIME,
        WITH_TIME
    }

    public static class OffsetTime {

        private int day;

        private int hour;

        private int minute;

        private LocalTime localTime;

        public OffsetTime(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        public OffsetTime(int day, LocalTime localTime) {
            this.day = day;
            this.localTime = localTime;
        }

        public int getDay() {
            return day;
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        public LocalTime getLocalTime() {
            return localTime;
        }
    }
}
