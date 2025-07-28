package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.repositories.StockRepository;
import com.burse.bursebackend.services.IStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockService implements IStockService {

    private final StockRepository stockRepository;

    @Transactional
    @Override
    public void updateStockPrice(Stock stock, BigDecimal newPrice) {
        stock.setCurrentPrice(newPrice);
        stockRepository.save(stock);
    }

    @Override
    public Optional<Stock> findById(String stockId) {
        return stockRepository.findById(stockId);
    }

    @Override
    public List<Stock> findAll() {
        return stockRepository.findAll();
    }


}
