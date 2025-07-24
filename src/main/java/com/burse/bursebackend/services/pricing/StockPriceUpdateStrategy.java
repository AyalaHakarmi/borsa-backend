package com.burse.bursebackend.services.pricing;

import com.burse.bursebackend.entities.Stock;

import java.math.BigDecimal;

public interface StockPriceUpdateStrategy {
    BigDecimal calculateNewPrice(Stock stock);
}
