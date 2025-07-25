package com.burse.bursebackend.services.impl.stocks;

import com.burse.bursebackend.dtos.stock.StockSimpleDTO;
import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.repositories.StockRepository;
import com.burse.bursebackend.services.stocks.IStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService implements IStockService {

    private final StockRepository stockRepository;

    @Override
    public void updateStockPrice(Stock stock, BigDecimal tradePricePerUnit) {
        stock.setCurrentPrice(tradePricePerUnit);
        stockRepository.save(stock);
    }

    public List<StockSimpleDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(Stock::toStockSimpleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Stock> findById(String stockId) {
        return stockRepository.findById(stockId);
    }


}
