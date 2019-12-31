package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gadjini.reminder.job.TimeZoneDbSenderJob;
import ru.gadjini.reminder.model.TimeZone;
import ru.gadjini.reminder.properties.TimeZoneDbProperties;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Service
public class TimezoneService {

    private static final String API = "http://api.timezonedb.com/v2.1/get-time-zone";

    private String apiKey;

    private RestTemplate restTemplate = new RestTemplate();

    private TimeZoneDbSenderJob timeZoneDbSenderJob;

    @Autowired
    public TimezoneService(TimeZoneDbProperties properties, TimeZoneDbSenderJob timeZoneDbSenderJob) {
        this.apiKey = properties.getKey();
        this.timeZoneDbSenderJob = timeZoneDbSenderJob;
    }

    public void getZoneId(double latitude, double longitude, Consumer<ZoneId> consumer) {
        Objects.requireNonNull(consumer);

        timeZoneDbSenderJob.pushJob(() -> {
            String url = buildUrl(Map.of("key", apiKey, "format", "json", "by", "position", "lat", latitude, "lng", longitude));
            TimeZone timeZone = restTemplate.getForObject(url, TimeZone.class);
            ZoneId zoneId = timeZone == null ? null : timeZone.isOk() ? ZoneId.of(timeZone.getZoneName()) : null;

            consumer.accept(zoneId);
        });
    }

    private String buildUrl(Map<String, Object> params) {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        params.forEach((s, o) -> {
            valueMap.putIfAbsent(s, new ArrayList<>());
            valueMap.get(s).add(o.toString());
        });

        return UriComponentsBuilder.fromHttpUrl(API)
                .queryParams(valueMap)
                .toUriString();
    }
}
