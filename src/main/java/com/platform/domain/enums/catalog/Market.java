package com.platform.domain.enums.catalog;


import java.util.Locale;

public enum Market {
    US(Currency.USD),
    MX(Currency.MXN);

    private final Currency currency;

    Market(Currency currency) {
        this.currency = currency;
    }

    public Currency currency() {
        return currency;
    }

    public static Market from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Market is required.");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Market must be US or MX.");
        }
    }
}
