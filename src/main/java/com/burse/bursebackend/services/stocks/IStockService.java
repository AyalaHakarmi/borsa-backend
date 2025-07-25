package com.burse.bursebackend.services.stocks;

import com.burse.bursebackend.dtos.stock.StockSimpleDTO;
import com.burse.bursebackend.entities.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IStockService {
    void updateStockPrice(Stock stock, BigDecimal tradePricePerUnit);

    List<StockSimpleDTO> getAllStocks();

    Optional<Stock> findById(String stockId);
}
