package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.dtos.StockDetailDTO;
import com.burse.bursebackend.dtos.TradeDTO;
import com.burse.bursebackend.dtos.TraderDTO;
import com.burse.bursebackend.dtos.offer.ActiveOfferResponseDTO;
import com.burse.bursebackend.dtos.stock.StockSimpleDTO;
import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trade;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.types.ErrorCode;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.services.IOfferService;
import com.burse.bursebackend.services.ITradeService;
import com.burse.bursebackend.services.ITraderService;
import com.burse.bursebackend.services.IStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class BurseViewService {

    private final IOfferService offerService;
    private final ITradeService tradeService;
    private final ITraderService traderService;
    private final IStockService stockService;

    public StockDetailDTO getStockDetails(String stockId) {

        Optional<Stock> stock = stockService.findById(stockId);
        if (stock.isEmpty()) {
            log.warn("Stock not found with id: {}. cannot provide stock details.", stockId);
            throw new BurseException(ErrorCode.STOCK_NOT_FOUND, "Stock not found with id: " + stockId);
        }

        return new StockDetailDTO(
                stock.get(),
                mapToOfferResponse(stockId, offerService::getActiveOffersForStock),
                get10RecentTradesForStock(stock.get()));
    }

    private List<TradeDTO> get10RecentTradesForStock(Stock stock) {
        return mapToTradeDTO(tradeService.get10RecentTradesForStock(stock));

    }

    public TraderDTO getTraderDetails(String traderId) {
        Optional<Trader> traderOpt = traderService.findById(traderId);
        if (traderOpt.isEmpty()) {
            log.warn("Trader not found with id: {}. cannot provide trader details.", traderId);
            throw new BurseException(ErrorCode.TRADER_NOT_FOUND, "Trader not found with id: " + traderId);
        }
        Trader trader = traderOpt.get();
        log.debug("Fetching trader details for traderId: {}", traderId);
        return new TraderDTO(trader , mapToOfferResponse(traderId, offerService::getActiveOffersForTrader));
    }

    public List<ActiveOfferResponseDTO> mapToOfferResponse(String id, Function<String, List<ActiveOffer>> fetchFunction) {
        return fetchFunction.apply(id)
                .stream()
                .map(ActiveOfferResponseDTO::new)
                .toList();
    }

    private List<TradeDTO> mapToTradeDTO(List<Trade> trades) {
        return trades.stream()
                .map(TradeDTO::new)
                .toList();
    }

    public List<StockSimpleDTO> getAllStocks() {
        return stockService.findAll().stream()
                .map(StockSimpleDTO::new)
                .toList();
    }

    public List<TradeDTO> get8RecentTradesForTrader(String traderId) {
        return mapToTradeDTO(tradeService.get8RecentTradesForTrader(traderId));

    }


}

