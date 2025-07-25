package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.dtos.StockDetailDTO;
import com.burse.bursebackend.dtos.TraderDTO;
import com.burse.bursebackend.dtos.offer.OfferResponseDTO;
import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.enums.ErrorCode;
import com.burse.bursebackend.enums.OfferType;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.services.IOfferService;
import com.burse.bursebackend.services.ITradeService;
import com.burse.bursebackend.services.ITraderService;
import com.burse.bursebackend.services.stocks.IStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataAggregatorService {

    private final IOfferService offerService;
    private final ITradeService tradeService;
    private final ITraderService traderService;
    private final IStockService stockService;


    public StockDetailDTO getStockDetails(String stockId) {
        Stock stock = stockService.findById(stockId)
                .orElseThrow(() -> new BurseException(ErrorCode.STOCK_NOT_FOUND, "Stock not found"));

        StockDetailDTO dto = stock.toStockDetailDTO();

        dto.setOffers(offerService.getActiveOffersForStock(stockId));
        dto.setRecentTrades(tradeService.getRecentTradesForStock(stock));

        return dto;
    }

    public TraderDTO getTraderDetails(String traderId) {
        Trader trader = traderService.findById(traderId)
                .orElseThrow(() -> new BurseException(ErrorCode.TRADER_NOT_FOUND, "Trader not found"));

        TraderDTO dto = trader.toDTO();
        dto.setActiveOffers(offerService.getActiveOffersForTrader(traderId));
        return dto;
    }



}

