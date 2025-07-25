package com.burse.bursebackend.services.stocks;

import com.burse.bursebackend.entities.Stock;

import java.math.BigDecimal;

public interface IStockPriceUpdateStrategy {
    BigDecimal calculateNewPrice(Stock stock);
}
