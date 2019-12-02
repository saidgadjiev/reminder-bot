package ru.gadjini.reminder.request;

import java.util.HashMap;
import java.util.Map;

public class RequestParams {

    private Map<String, String> params = new HashMap<>();

    public String getString(String key) {
        return params.get(key);
    }

    public Integer getInt(String key) {
        return Integer.parseInt(params.get(key));
    }

    public void add(String key, String value) {
        params.put(key, value);
    }

    public void add(String key, Integer value) {
        params.put(key, String.valueOf(value));
    }

    public void add(String key, Boolean value) {
        params.put(key, String.valueOf(value));
    }

    public boolean contains(String key) {
        return params.containsKey(key);
    }

    public String serialize(String delimiter) {
        StringBuilder serialize = new StringBuilder();

        params.forEach((s, s2) -> serialize.append(s).append(delimiter).append(s2));

        return serialize.toString();
    }
}
