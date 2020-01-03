package ru.gadjini.reminder.samples;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneOffset;

public class ObjectMapperSample {

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(new JavaTimeModule(), new JodaModule());
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        DateTime dateTime = new DateTime(ZoneOffset.UTC);

        String json = objectMapper.writeValueAsString(dateTime);

        System.out.println(json);

        objectMapper.readValue(json, Object.class);

        System.out.println("YES");
    }
}
