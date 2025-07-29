package com.burse.bursebackend.services;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ITraderService {

    void verifyTraderStockAvailability(SellOffer sellOffer, int numOfStocksTraded, String lockTraderMoney, String lockTraderStock);

    void verifyTraderMoneyAvailable(BuyOffer buyOffer, BigDecimal tradeTotalPrice, String lockTraderMoney);

    void updateTradersMoney(BuyOffer buyOffer, SellOffer sellOffer, BigDecimal tradeTotalPrice);

    void updateTradersStock(BuyOffer buyOffer, SellOffer sellOffer, int numOfStocksTraded);

    List<String> getAllTraderNames();

    Optional<Trader> findById(String traderId);

    void exchangeMoneyAndStock(BuyOffer buyOffer, SellOffer sellOffer, Stock stock, BigDecimal tradeTotalPrice, int numOfStocksTraded);

}
