package com.burse.bursebackend.services.stocks;

import com.burse.bursebackend.entities.Stock;

import java.math.BigDecimal;

public interface IStockService {
    void updateStockPrice(Stock stock, BigDecimal tradePricePerUnit);
}
