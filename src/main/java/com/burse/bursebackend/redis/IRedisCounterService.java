package com.burse.bursebackend.redis;

import com.burse.bursebackend.types.OfferType;

public interface IRedisCounterService {
    boolean tryAddOffer(String traderId, String stockId, OfferType type);

    boolean removeOffer(String traderId, String stockId, OfferType type);
}
