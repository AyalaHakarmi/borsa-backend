package com.burse.bursebackend.services.impl.stocks;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.repositories.StockRepository;
import com.burse.bursebackend.services.stocks.IStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StockService implements IStockService {

    private final StockRepository stockRepository;

    @Override
    public void updateStockPrice(Stock stock, BigDecimal tradePricePerUnit) {
        stock.setCurrentPrice(tradePricePerUnit);
        stockRepository.save(stock);
    }
}
