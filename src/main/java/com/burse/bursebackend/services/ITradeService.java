package com.burse.bursebackend.services;

import com.burse.bursebackend.dtos.TradeDTO;
import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;

import java.util.List;

public interface ITradeService {

    public void executeTrade(BuyOffer buyOffer, SellOffer sellOffer, int tradeQty);

    List<TradeDTO> getRecentTradesForTrader(String traderId);

    List<TradeDTO> getRecentTradesForStock(Stock stock);
}
