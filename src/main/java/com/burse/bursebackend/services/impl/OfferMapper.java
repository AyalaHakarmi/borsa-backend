package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.dtos.offer.BaseOfferDTO;
import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.burse.bursebackend.dtos.offer.SellOfferDTO;
import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.services.IStockService;
import com.burse.bursebackend.services.ITraderService;
import com.burse.bursebackend.types.ErrorCode;
import com.burse.bursebackend.types.OfferType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class OfferMapper {

    private final ITraderService traderService;
    private final IStockService stockService;

    public Pair<ActiveOffer, OfferType> buildOfferFromDTO(BaseOfferDTO offerDTO) {
        Optional<Trader> traderOpt = traderService.findById(offerDTO.getTraderId());
        if (traderOpt.isEmpty()) {
            log.warn("Trader not found with id: {}. The offer was rejected.", offerDTO.getTraderId());
            throw new BurseException(ErrorCode.TRADER_NOT_FOUND, "Trader not found with id: " + offerDTO.getTraderId());
        }
        Trader trader = traderOpt.get();
        Optional<Stock> stockOpt = stockService.findById(offerDTO.getStockId());
        if (stockOpt.isEmpty()) {
            log.warn("Stock not found with id: {}. The offer was rejected.", offerDTO.getStockId());
            throw new BurseException(ErrorCode.STOCK_NOT_FOUND, "Stock not found with id: " + offerDTO.getStockId());
        }
        Stock stock = stockOpt.get();

        if (offerDTO instanceof BuyOfferDTO) {
            return Pair.of(new BuyOffer(trader, stock, offerDTO.getPrice(), offerDTO.getAmount()), OfferType.BUY);
        }

        if (offerDTO instanceof SellOfferDTO) {
            return Pair.of(new SellOffer(trader, stock, offerDTO.getPrice(), offerDTO.getAmount()), OfferType.SELL);
        }

        throw new BurseException(ErrorCode.INVALID_OFFER, "Unknown offer type");
    }


}
