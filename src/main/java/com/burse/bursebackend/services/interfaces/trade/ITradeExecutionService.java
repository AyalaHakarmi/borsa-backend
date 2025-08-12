package com.burse.bursebackend.services.interfaces.trade;

import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface ITradeExecutionService {
    @Transactional
    int executeTrade(BuyOffer buyOffer, SellOffer sellOffer);

    void recordTrade(BuyOffer buyOffer, SellOffer sellOffer, BigDecimal tradePricePerUnit, int numOfStocksTraded);
}
