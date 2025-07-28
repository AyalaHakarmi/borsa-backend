package com.burse.bursebackend.pricing;

import com.burse.bursebackend.entities.Stock;
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

        double volatilityFactor = random.nextDouble() < 0.05
                ? (0.02 + random.nextDouble() * 0.02)
                : (0.002 + random.nextDouble() * 0.008);

        boolean positive = random.nextBoolean();
        BigDecimal multiplier = BigDecimal.valueOf(1 + (positive ? volatilityFactor : -volatilityFactor));

        BigDecimal newPrice = currentPrice.multiply(multiplier);
        return newPrice.max(BigDecimal.valueOf(0.01)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescription() {
        return "Updates stock price using purely random fluctuations, simulating unpredictable market behavior with small and occasional larger shifts.";
    }




}


