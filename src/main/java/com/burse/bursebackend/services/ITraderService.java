package com.burse.bursebackend.services;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ITraderService {
    boolean hasEnoughMoney(Trader trader, BigDecimal tradeTotalPrice);

    boolean hasEnoughStock(Trader trader, Stock stock, int tradeQty);

    void updateTradersMoney(BuyOffer buyOffer, SellOffer sellOffer, BigDecimal tradeTotalPrice);

    void updateTradersStock(BuyOffer buyOffer, SellOffer sellOffer, int tradeQty);

    List<String> getAllTraderNames();

    Optional<Trader> findById(String traderId);
}
