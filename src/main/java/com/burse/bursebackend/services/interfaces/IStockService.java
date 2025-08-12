package com.burse.bursebackend.services.interfaces;

import com.burse.bursebackend.entities.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IStockService {
    void updateStockPrice(Stock stock, BigDecimal tradePricePerUnit);

    Optional<Stock> findById(String stockId);

    List<Stock> findAll();
}
