package ru.gadjini.reminder.model;

public class WebMoneyPayment {

    private String payeePurse;

    private String secretKey;

    private double paymentAmount;

    private int userId;

    private int planId;

    public String payeePurse() {
        return this.payeePurse;
    }

    public String secretKey() {
        return this.secretKey;
    }

    public double paymentAmount() {
        return this.paymentAmount;
    }

    public int userId() {
        return this.userId;
    }

    public int planId() {
        return this.planId;
    }

    public WebMoneyPayment payeePurse(final String payeePurse) {
        this.payeePurse = payeePurse;
        return this;
    }

    public WebMoneyPayment secretKey(final String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public WebMoneyPayment paymentAmount(final double paymentAmount) {
        this.paymentAmount = paymentAmount;
        return this;
    }

    public WebMoneyPayment userId(final int userId) {
        this.userId = userId;
        return this;
    }

    public WebMoneyPayment planId(final int planId) {
        this.planId = planId;
        return this;
    }
}
