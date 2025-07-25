package com.burse.bursebackend.services.impl.stocks;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.repositories.StockRepository;
import com.burse.bursebackend.services.stocks.IStockPriceUpdater;
import com.burse.bursebackend.services.stocks.IStockPriceUpdateStrategy;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StockPriceUpdater implements IStockPriceUpdater {

    private static final long UPDATE_INTERVAL_MS = 60_000L;

    private final StockRepository stockRepository;
    private final Map<String, IStockPriceUpdateStrategy> strategyMap;

    private volatile IStockPriceUpdateStrategy currentStrategy;

    public StockPriceUpdater(StockRepository stockRepository,
                             Map<String, IStockPriceUpdateStrategy> strategyMap,
                             @Value("${burse.strategy.default:comprehensiveStrategy}") String defaultStrategyName) {
        this.stockRepository = stockRepository;
        this.strategyMap = strategyMap;
        this.currentStrategy = strategyMap.getOrDefault(
                defaultStrategyName,
                strategyMap.values().iterator().next()
        );
    }

    @Override
    @Scheduled(fixedRate = UPDATE_INTERVAL_MS)
    @Transactional
    public void updateAllPrices() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            int retries = 3;
            boolean updated = false;

            while (!updated && retries-- > 0) {
                try {
                    BigDecimal newPrice = currentStrategy.calculateNewPrice(stock);
                    stock.setCurrentPrice(newPrice);
                    stockRepository.save(stock);
                    updated = true;
                } catch (OptimisticLockException e) {
                    System.out.printf("Conflict updating stock %s, retrying...%n", stock.getId());
                    stock = stockRepository.findById(stock.getId()).orElse(null);
                    if (stock == null) break;
                }
            }

            if (!updated) {
                System.out.printf("Failed to update stock %s after retries.%n", stock.getId());
            }
        }

        System.out.printf("Stock prices updated using strategy: %s%n", getCurrentStrategyName());
    }

    @Override
    public void setStrategyByName(String name) {
        if (strategyMap.containsKey(name)) {
            this.currentStrategy = strategyMap.get(name);
            System.out.printf("Strategy changed to: %s%n", name);
        } else {
            throw new IllegalArgumentException("Unknown strategy: " + name);
        }
    }

    @Override
    public String getCurrentStrategyName() {
        return strategyMap.entrySet().stream()
                .filter(entry -> entry.getValue() == currentStrategy)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("unknown");
    }

    @Override
    public Set<String> getAvailableStrategyNames() {
        return strategyMap.keySet();
    }
}
