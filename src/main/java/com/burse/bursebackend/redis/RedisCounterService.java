package com.burse.bursebackend.redis;

import com.burse.bursebackend.types.KeyType;
import com.burse.bursebackend.types.OfferType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCounterService {

    private final RedissonClient redisson;


    private static final String LUA_INSERT = """
    -- KEYS[1]=hashKey, ARGV[1]=thisField, ARGV[2]=otherField, ARGV[3]=inc("1")
    local other = tonumber(redis.call('HGET', KEYS[1], ARGV[2]) or '0')
    if other > 0 then return -1 end
    local newVal = tonumber(redis.call('HINCRBY', KEYS[1], ARGV[1], tonumber(ARGV[3])))
    return newVal
    """;

    private static final String LUA_DELETE = """
    -- KEYS[1]=hashKey, ARGV[1]=field, ARGV[2]=dec("1")
    local cur = tonumber(redis.call('HGET', KEYS[1], ARGV[1]) or '0')
    if cur <= 0 then return -1 end
    local newVal = tonumber(redis.call('HINCRBY', KEYS[1], ARGV[1], -tonumber(ARGV[2])))
    if newVal <= 0 then redis.call('HDEL', KEYS[1], ARGV[1]) end
    local buy = tonumber(redis.call('HGET', KEYS[1], 'buy') or '0')
    local sell = tonumber(redis.call('HGET', KEYS[1], 'sell') or '0')
    if buy == 0 and sell == 0 then redis.call('DEL', KEYS[1]) end
    return newVal
    """;

    public boolean tryAddOffer(String traderId, String stockId, OfferType type) {
        String key = KeyBuilder.buildKey(KeyType.OFFER_TYPE, traderId, stockId);
        String thisField  = type.field();
        String otherField = type.opposite().field();

        Number res = redisson.getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                LUA_INSERT,
                RScript.ReturnType.INTEGER,
                java.util.Collections.singletonList(key),
                thisField,                    // ARGV[1] - "buy"/"sell"
                otherField,                   // ARGV[2] - "sell"/"buy"
                "1"                           // ARGV[3] - מחרוזת "1"
        );
        return res.longValue() >= 0;     // -1 => OPPOSITE_OFFER_EXISTS
    }

    public boolean removeOffer(String traderId, String stockId, OfferType type) {
        String key = KeyBuilder.buildKey(KeyType.OFFER_TYPE, traderId, stockId);
        String field = type.field();

        Number res = redisson.getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                LUA_DELETE,
                RScript.ReturnType.INTEGER,
                java.util.Collections.singletonList(key),
                field,                        // ARGV[1]
                "1"                           // ARGV[2]
        );
        boolean ok = res.longValue() >= 0;
        if (!ok) {
            log.error("Failed to remove offer for traderId={}, stockId={}, type={}. No offer found or already removed.",
                    traderId, stockId, type);
        }
        return ok;
    }
}
