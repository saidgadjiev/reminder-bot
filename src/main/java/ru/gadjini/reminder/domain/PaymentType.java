package ru.gadjini.reminder.domain;

public enum PaymentType {

    CARD(4),
    BEELINE(12);

    private final int type;

    PaymentType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static PaymentType fromType(int type) {
        for (PaymentType paymentType : values()) {
            if (paymentType.type == type) {
                return paymentType;
            }
        }

        throw new IllegalArgumentException();
    }
}
