package com.burse.bursebackend.config;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.repositories.StockRepository;
import com.burse.bursebackend.repositories.TraderRepository;
import com.burse.bursebackend.repositories.offer.SellOfferRepository;
import com.burse.bursebackend.types.RedisPrefixes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BurseInitializer {

    private static final String BURSE_TRADER_ID = "BURSE";

    private final StockRepository stockRepository;
    private final TraderRepository traderRepository;
    private final SellOfferRepository sellOfferRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedissonClient redisson;

    @PostConstruct
    public void initialData() throws Exception {

        cleanRedis();

        InputStream jsonStream = getClass().getResourceAsStream("/data/BurseJson.json");
        JsonBootstrapData data = objectMapper.readValue(jsonStream, JsonBootstrapData.class);

        List<Stock> stocks = data.getShares();
        List<Trader> traders = data.getTraders();

        stockRepository.saveAll(stocks);
        traderRepository.saveAll(traders);

        log.info("Loaded stocks and traders from JSON.");

        Trader burse = new Trader();
        burse.setId(BURSE_TRADER_ID);
        burse.setName("Burse");
        burse.setMoney(BigDecimal.ZERO);
        burse.setHoldings(stocks.stream()
                .collect(java.util.stream.Collectors.toMap(Stock::getId, Stock::getAmount)));

        traderRepository.save(burse);

        for (Stock stock : stocks) {
            SellOffer offer = new SellOffer(
                    burse,
                    stock,
                    stock.getCurrentPrice(),
                    stock.getAmount()
            );
            sellOfferRepository.save(offer);
            burse.addOffer(offer);
            stock.addOffer(offer);
        }

        log.info("Initial sell offers were opened by the Burse.");

    }

    private void cleanRedis() {
        RKeys keys = redisson.getKeys();
        long deleted = 0;
        deleted += keys.deleteByPattern(RedisPrefixes.OFFER_COUNTERS + "*");
        deleted += keys.deleteByPattern(RedisPrefixes.LOCKS + "*");


        log.info("Redis pattern cleanup on startup: deleted {} keys", deleted);
    }

}
