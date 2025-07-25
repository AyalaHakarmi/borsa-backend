package com.burse.bursebackend.mappers;


import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.burse.bursebackend.dtos.offer.SellOfferDTO;
import com.burse.bursebackend.dtos.offer.BaseOfferDTO;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.exceptions.ErrorCode;
import com.burse.bursebackend.repositories.StockRepository;
import com.burse.bursebackend.repositories.TraderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OfferMapper  {

    private final TraderRepository traderRepository;
    private final StockRepository stockRepository;


    public ActiveOffer fromDto(BaseOfferDTO dto) {
        Trader trader = traderRepository.findById(dto.getTraderId())
                .orElseThrow(() -> new BurseException(ErrorCode.TRADER_NOT_FOUND, "Trader not found"));

        Stock stock = stockRepository.findById(dto.getStockId())
                .orElseThrow(() -> new BurseException(ErrorCode.STOCK_NOT_FOUND, "Stock not found"));

        if (dto instanceof BuyOfferDTO) {
            return new BuyOffer(trader, stock, dto.getPrice(), dto.getAmount());
        }

        if (dto instanceof SellOfferDTO) {
            return new SellOffer(trader, stock, dto.getPrice(), dto.getAmount());
        }

        throw new BurseException(ErrorCode.INVALID_OFFER, "Unknown offer type");
    }
}

