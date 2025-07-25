package com.burse.bursebackend.entities.offer;

public enum OfferType {
    BUY,
    SELL;

    public OfferType opposite() {
        return this == BUY ? SELL : BUY;
    }
}
