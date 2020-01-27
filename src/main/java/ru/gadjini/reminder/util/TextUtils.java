package ru.gadjini.reminder.util;

public class TextUtils {

    private TextUtils() {

    }

    public static String removeHtmlTags(String str) {
        return str.replaceAll("<.*?>", "");
    }
}
