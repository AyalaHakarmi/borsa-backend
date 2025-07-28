package com.burse.bursebackend.types;

public enum OfferType {
    BUY,
    SELL;

    public OfferType opposite() {
        return this == BUY ? SELL : BUY;
    }
}
