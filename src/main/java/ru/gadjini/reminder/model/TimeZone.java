package ru.gadjini.reminder.model;

public class TimeZone {

    private String status;

    private String message;

    private String zoneName;

    public boolean isOk() {
        return "OK".equals(status);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }
}
