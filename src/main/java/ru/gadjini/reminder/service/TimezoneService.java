package ru.gadjini.reminder.service;

import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Service
public class TimezoneService {

    private TimeZoneEngine timeZoneEngine;

    @Autowired
    public TimezoneService(TimeZoneEngine timeZoneEngine) {
        this.timeZoneEngine = timeZoneEngine;
    }

    public ZoneId getZoneId(double latitude, double longitude) {
        return timeZoneEngine.query(latitude, longitude).orElseThrow();
    }
}
