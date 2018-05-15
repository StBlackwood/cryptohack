package com.blackwood.cryptohack;

/**
 * Created by blackwood on 2/5/18.
 */

public class MassageEvent {
    private final double value;
    private final String fromCurrency;
    private final String toCurrency;

    public MassageEvent(double value, String fromCurrency, String toCurrency) {
        this.value = value;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
    }

    public double getValue() {
        return value;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }
}
