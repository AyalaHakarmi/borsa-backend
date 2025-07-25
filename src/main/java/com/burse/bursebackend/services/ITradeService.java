package com.burse.bursebackend.services;

import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;

public interface ITradeService {

    public void executeTrade(BuyOffer buyOffer, SellOffer sellOffer, int tradeQty);

}
