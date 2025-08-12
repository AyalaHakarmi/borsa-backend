package com.burse.bursebackend.locks;

import com.burse.bursebackend.types.KeyType;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class KeyBuilder {

    public static String buildKey(KeyType type, String... parts) {
        return switch (type) {
            case OFFER -> {
                validate(parts, 1, "OFFER key requires: offerId");
                yield "lock:offerId:" + parts[0];
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
                validate(parts, 2, "OFFER_TYPE key requires: traderId, stockId");
                yield "ctr:offers:trader:" + parts[0] + ":stock:" + parts[1];
            }
        };
    }


    private static void validate(String[] parts, int expected, String msg) {
        if (parts.length != expected) {
            throw new IllegalArgumentException("Expected " + expected + " parameters: " + msg + ". Got " + parts.length);
        }
    }
}
