package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ReminderTextBuilder {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public String create(String text, LocalDateTime remindAt) {
        return text + " " + DATE_TIME_FORMATTER.format(remindAt);
    }
}
