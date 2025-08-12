package com.burse.bursebackend.pricing;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.types.ErrorCode;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.services.interfaces.IStockService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockPriceService implements IStockPriceService {

    private final Map<String, IStockPriceUpdateStrategy> strategyMap;
    private final IStockService stockService;
    private volatile IStockPriceUpdateStrategy currentStrategy;

    public StockPriceService(Map<String, IStockPriceUpdateStrategy> strategyMap,
                             @Value("${burse.strategy.default:comprehensiveStrategy}") String defaultStrategyName, IStockService stockService) {
        this.strategyMap = strategyMap;
        this.currentStrategy = strategyMap.getOrDefault(
                defaultStrategyName,
                strategyMap.values().iterator().next()
        );
        this.stockService = stockService;
    }

    @Scheduled(fixedRate = UPDATE_INTERVAL_MS)
    @Transactional
    @Override
    public void updateAllPrices() {
        List<Stock> stocks = stockService.findAll();

        for (Stock stock : stocks) {
            try {
                BigDecimal newPrice = currentStrategy.calculateNewPrice(stock);
                stockService.updateStockPrice(stock, newPrice);

            } catch (Exception e) {
                log.debug("Failed to update stock {}: {}", stock.getId(), e.getMessage(), e);
            }
        }

        log.info("Stock prices updated using strategy: {}", getCurrentStrategy());
    }


    @Override
    public String switchStrategy(String strategyName) {
        if (Objects.equals(strategyName, getCurrentStrategy())){
            log.info("Already using strategy: {}", strategyName);
            return "Already using strategy: " + strategyName;
        }
        if (strategyMap.containsKey(strategyName)) {
            this.currentStrategy = strategyMap.get(strategyName);
            log.info("Strategy changed to: {}", strategyName);
        } else {
            log.warn("Unknown strategy: {}", strategyName);
            throw new BurseException(ErrorCode.UNKNOWN_STRATEGY, "Unknown strategy: " + strategyName);
        }
        return "Strategy changed to: " + strategyName;
    }

    @Override
    public String getCurrentStrategy() {
        log.debug("Current strategy: {}", currentStrategy.getClass().getSimpleName());
        return strategyMap.entrySet().stream()
                .filter(entry -> entry.getValue() == currentStrategy)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("unknown");
    }

    @Override
    public Map<String, String> getAvailableStrategies() {
        return strategyMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getDescription()
                ));
    }

}
