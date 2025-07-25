package com.burse.bursebackend.enums;

public enum OfferType {
    BUY,
    SELL;

    public OfferType opposite() {
        return this == BUY ? SELL : BUY;
    }
}
