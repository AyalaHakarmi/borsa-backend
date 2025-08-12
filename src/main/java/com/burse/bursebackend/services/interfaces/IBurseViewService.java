package com.burse.bursebackend.services.interfaces;

import com.burse.bursebackend.dtos.TradeDTO;
import com.burse.bursebackend.dtos.TraderDTO;
import com.burse.bursebackend.dtos.offer.ActiveOfferResponseDTO;
import com.burse.bursebackend.dtos.stock.StockDetailDTO;
import com.burse.bursebackend.dtos.stock.StockSimpleDTO;
import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trade;
import com.burse.bursebackend.entities.offer.ActiveOffer;

import java.util.List;
import java.util.function.Function;

public interface IBurseViewService {
    StockDetailDTO getStockDetails(String stockId);

    List<TradeDTO> get10RecentTradesForStock(Stock stock);

    TraderDTO getTraderDetails(String traderId);

    List<ActiveOfferResponseDTO> mapToOfferResponse(String id, Function<String, List<ActiveOffer>> fetchFunction);

    List<TradeDTO> mapToTradeDTO(List<Trade> trades);

    List<StockSimpleDTO> getAllStocks();

    List<TradeDTO> get8RecentTradesForTrader(String traderId);
}
