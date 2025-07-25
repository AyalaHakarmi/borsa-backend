package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.services.ITraderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TraderService implements ITraderService {


    @Override
    public boolean hasEnoughMoney(Trader trader, BigDecimal tradeTotalPrice) {
        if (trader == null || tradeTotalPrice == null) {
            return false;
        }
        BigDecimal traderMoney = trader.getMoney();
        return traderMoney != null && traderMoney.compareTo(tradeTotalPrice) >= 0;
    }

    @Override
    public boolean hasEnoughStock(Trader trader, Stock stock, int tradeQty) {
        if (trader == null || stock == null || tradeQty <= 0) {
            return false;
        }

        Integer traderStockAmount = trader.getHoldings().get(stock.getId());

        if (traderStockAmount == null) {
            return false;
        }

        return traderStockAmount >= tradeQty;
    }

    @Override
    public void updateTradersMoney(BuyOffer buyOffer, SellOffer sellOffer, BigDecimal tradeTotalPrice) {
        Trader buyer = buyOffer.getTrader();
        Trader seller = sellOffer.getTrader();

        if (buyer != null && hasEnoughMoney(buyer, tradeTotalPrice)) {
            buyer.setMoney(buyer.getMoney().subtract(tradeTotalPrice));
        }

        if (seller != null) {
            seller.setMoney(seller.getMoney().add(tradeTotalPrice));
        }

    }

    @Override
    public void updateTradersStock(BuyOffer buyOffer, SellOffer sellOffer, int tradeQty) {
        Trader buyer = buyOffer.getTrader();
        Trader seller = sellOffer.getTrader();
        Stock stock = buyOffer.getStock();

        if (buyer != null) {
            Integer buyerStockAmount = buyer.getHoldings().getOrDefault(stock.getId(), 0);
            buyer.getHoldings().put(stock.getId(), buyerStockAmount + tradeQty);
        }

        if (seller != null) {
            Integer sellerStockAmount = seller.getHoldings().getOrDefault(stock.getId(), 0);
            seller.getHoldings().put(stock.getId(), sellerStockAmount - tradeQty);
        }

    }

}
