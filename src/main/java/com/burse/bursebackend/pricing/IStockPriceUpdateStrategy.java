package com.burse.bursebackend.pricing;

import com.burse.bursebackend.entities.Stock;

import java.math.BigDecimal;

public interface IStockPriceUpdateStrategy {
    BigDecimal calculateNewPrice(Stock stock);

    String getDescription();
}
