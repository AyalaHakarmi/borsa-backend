package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trade;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.repositories.TradeRepository;
import com.burse.bursebackend.services.IStockService;
import com.burse.bursebackend.services.ITradeExecutionService;
import com.burse.bursebackend.services.ITraderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Service
public class TradeExecutionService implements ITradeExecutionService {

    private final ITraderService traderService;
    private final TradeRepository tradeRepository;
    private final IStockService stockService;

    @Transactional
    @Override
    public int executeTrade(BuyOffer buyOffer, SellOffer sellOffer) {
        log.info("Potential trade found: BUY {} ⇄ SELL {}", buyOffer.getId(), sellOffer.getId());
        int numOfStocksTraded = Math.min(buyOffer.getAmount(), sellOffer.getAmount());
        BigDecimal tradePricePerUnit = getTradePrice(buyOffer, sellOffer);
        BigDecimal tradeTotalPrice = tradePricePerUnit.multiply(BigDecimal.valueOf(numOfStocksTraded));
        Stock stock = sellOffer.getStock();

        traderService.exchangeMoneyAndStock(buyOffer, sellOffer, stock, tradeTotalPrice, numOfStocksTraded);

        recordTrade(buyOffer, sellOffer, tradePricePerUnit ,numOfStocksTraded );

        stockService.updateStockPrice(stock, tradePricePerUnit);
        return numOfStocksTraded;

    }

    @Override
    public void recordTrade(BuyOffer buyOffer, SellOffer sellOffer, BigDecimal tradePricePerUnit, int numOfStocksTraded) {
        Trade trade = new Trade(buyOffer, sellOffer, tradePricePerUnit, numOfStocksTraded);
        tradeRepository.save(trade);
        buyOffer.getTrader().addAsBuyerTrade(trade);
        sellOffer.getTrader().addAsSellerTrade(trade);
        buyOffer.getStock().addTrade(trade);
    }

    private BigDecimal getTradePrice(BuyOffer buyOffer, SellOffer sellOffer) {
        if (buyOffer.getCreatedAt().isBefore(sellOffer.getCreatedAt())) {
            return buyOffer.getPrice();
        } else {
            return sellOffer.getPrice();
        }
    }
}
