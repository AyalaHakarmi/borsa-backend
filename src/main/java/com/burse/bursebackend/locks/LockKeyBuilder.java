package com.burse.bursebackend.locks;

import com.burse.bursebackend.types.OfferType;
import com.burse.bursebackend.types.LockKeyType;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LockKeyBuilder {

    public static String buildKey(LockKeyType type, String... parts) {
        return switch (type) {
            case OFFER -> {
                validate(parts, 1, "OFFER key requires: offerId");
                yield "lock:offerId:" + parts[0];
            }
            case META -> {
                validate(parts, 2, "META key requires: traderId, stockId");
                yield "lock:meta:trader:" + parts[0] + ":stock:" + parts[1];
            }
            case MONEY -> {
                validate(parts, 1, "MONEY key requires: traderId");
                yield "lock:trader:" + parts[0] + ":money";
            }
            case STOCK -> {
                validate(parts, 2, "STOCK key requires: traderId, stockId");
                yield "lock:trader:" + parts[0] + ":stock:" + parts[1];
            }
            case OFFER_TYPE -> {
                throw new IllegalArgumentException("Use the overloaded method with OfferType for OFFER_TYPE");
            }
        };
    }

    public static String buildKey(LockKeyType type, String traderId, String stockId, OfferType offerType) {
        if (type != LockKeyType.OFFER_TYPE) {
            throw new IllegalArgumentException("This overload is only valid for OFFER_TYPE");
        }
        return "lock:trader:" + traderId + ":cant_insert:" + offerType.name() + "on_stock:" + stockId ;
    }

    private static void validate(String[] parts, int expected, String msg) {
        if (parts.length != expected) {
            throw new IllegalArgumentException("Expected " + expected + " parameters: " + msg + ". Got " + parts.length);
        }
    }
}
