package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.dtos.stock.StockDetailDTO;
import com.burse.bursebackend.dtos.TradeDTO;
import com.burse.bursebackend.dtos.TraderDTO;
import com.burse.bursebackend.dtos.offer.ActiveOfferResponseDTO;
import com.burse.bursebackend.dtos.stock.StockSimpleDTO;
import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trade;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.services.interfaces.IBurseViewService;
import com.burse.bursebackend.types.ErrorCode;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.services.interfaces.offer.IOfferService;
import com.burse.bursebackend.services.interfaces.trade.ITradeService;
import com.burse.bursebackend.services.interfaces.ITraderService;
import com.burse.bursebackend.services.interfaces.IStockService;
import com.burse.bursebackend.types.OfferType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class BurseViewService implements IBurseViewService {

    private final IOfferService offerService;
    private final ITradeService tradeService;
    private final ITraderService traderService;
    private final IStockService stockService;

    @Override
    public StockDetailDTO getStockDetails(String stockId) {

        Optional<Stock> stockOpt = stockService.findById(stockId);
        if (stockOpt.isEmpty()) {
            log.warn("Stock not found with id: {}. cannot provide stock details.", stockId);
            throw new BurseException(ErrorCode.STOCK_NOT_FOUND, "Stock not found with id: " + stockId);
        }
        Stock stock = stockOpt.get();
        return new StockDetailDTO(
                stock.getId(),
                stock.getName(),
                stock.getCurrentPrice(),
                stock.getAmount(),
                mapToOfferResponse(stockId, offerService::getActiveOffersForStock),
                get10RecentTradesForStock(stock));
    }

    @Override
    public List<TradeDTO> get10RecentTradesForStock(Stock stock) {
        return mapToTradeDTO(tradeService.get10RecentTradesForStock(stock));

    }

    @Override
    public TraderDTO getTraderDetails(String traderId) {
        Optional<Trader> traderOpt = traderService.findById(traderId);
        if (traderOpt.isEmpty()) {
            log.warn("Trader not found with id: {}. cannot provide trader details.", traderId);
            throw new BurseException(ErrorCode.TRADER_NOT_FOUND, "Trader not found with id: " + traderId);
        }
        Trader trader = traderOpt.get();
        log.debug("Fetching trader details for traderId: {}", traderId);
        return new TraderDTO(
                trader.getId() ,
                trader.getName(),
                trader.getMoney(),
                trader.getHoldings(),
                mapToOfferResponse(traderId, offerService::getActiveOffersForTrader)
        );
    }

    @Override
    public List<ActiveOfferResponseDTO> mapToOfferResponse(String id, Function<String, List<ActiveOffer>> fetchFunction) {
        return fetchFunction.apply(id)
                .stream()
                .map(offer -> {
                    assert offer.getStock() != null;
                    return new ActiveOfferResponseDTO(
                            offer.getTrader() != null? offer.getTrader().getId() : null,
                            offer.getStock().getId(),
                            offer.getPrice(),
                            offer.getAmount(),
                            offer.getId(),
                            offer instanceof BuyOffer? OfferType.BUY : OfferType.SELL,
                            offer.getCreatedAt(),
                            offer.getStock().getName()
                    );
                })
                .toList();
    }

    @Override
    public List<TradeDTO> mapToTradeDTO(List<Trade> trades) {
        return trades.stream()
                .map(trade -> new TradeDTO(
                        trade.getId(),
                        trade.getStock().getId(),
                        trade.getStock().getName(),
                        trade.getPricePerUnit(),
                        trade.getTotalPrice(),
                        trade.getAmount(),
                        trade.getTimestamp(),

                        trade.getBuyer().getId(),
                        trade.getBuyer().getName(),

                        trade.getSeller().getId(),
                        trade.getSeller().getName()
                ))
                .toList();
    }

    @Override
    public List<StockSimpleDTO> getAllStocks() {
        return stockService.findAll().stream()
                .map(stock -> new StockSimpleDTO(
                        stock.getId(),
                        stock.getName(),
                        stock.getCurrentPrice(),
                        stock.getAmount()))
                .toList();
    }

    @Override
    public List<TradeDTO> get8RecentTradesForTrader(String traderId) {
        return mapToTradeDTO(tradeService.get8RecentTradesForTrader(traderId));

    }


}

