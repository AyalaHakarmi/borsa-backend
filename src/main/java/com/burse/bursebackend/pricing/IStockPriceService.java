package com.burse.bursebackend.pricing;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;

public interface IStockPriceService {

     long UPDATE_INTERVAL_MS = 600_000L;

    @Scheduled(fixedRate = StockPriceService.UPDATE_INTERVAL_MS)
    @Transactional
    void updateAllPrices();

    String switchStrategy(String name);
    String getCurrentStrategy();
    Map<String, String> getAvailableStrategies();


}
