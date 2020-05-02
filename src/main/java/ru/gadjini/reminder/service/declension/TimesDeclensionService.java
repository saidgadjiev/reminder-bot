package ru.gadjini.reminder.service.declension;

import org.springframework.stereotype.Service;

@Service
public class TimesDeclensionService {

    public String getTimes(int count) {
        if (count >= 2 && count <= 4) {
            return "раза";
        }

        return "раз";
    }
}
