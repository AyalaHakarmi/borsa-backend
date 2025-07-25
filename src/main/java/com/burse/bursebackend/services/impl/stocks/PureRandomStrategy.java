package com.burse.bursebackend.services.impl.stocks;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.services.stocks.IStockPriceUpdateStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component("pureRandomStrategy")
public class PureRandomStrategy implements IStockPriceUpdateStrategy {

    private final Random random = new Random();

    @Override
    public BigDecimal calculateNewPrice(Stock stock) {
        BigDecimal currentPrice = stock.getCurrentPrice();

        double volatilityFactor = random.nextDouble() < 0.1
                ? (0.15 + random.nextDouble() * 0.35)
                : (0.005 + random.nextDouble() * 0.02);

        boolean positive = random.nextBoolean();
        BigDecimal multiplier = BigDecimal.valueOf(1 + (positive ? volatilityFactor : -volatilityFactor));

        BigDecimal newPrice = currentPrice.multiply(multiplier);
        return newPrice.max(BigDecimal.valueOf(0.01)).setScale(2, RoundingMode.HALF_UP);
    }
}

