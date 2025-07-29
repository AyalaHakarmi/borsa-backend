package com.burse.bursebackend.services;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trade;
import com.burse.bursebackend.entities.offer.ActiveOffer;

import java.util.List;

public interface ITradeService {

    void searchPotentialTrade(ActiveOffer newOffer);

    List<Trade> get10RecentTradesForStock(Stock stock);

    List<Trade> get8RecentTradesForTrader(String traderId);
}
