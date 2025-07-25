package com.burse.bursebackend.entities.offer;

import com.burse.bursebackend.dtos.offer.OfferResponseDTO;
import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.enums.OfferType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "active_offer")
@NoArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ActiveOffer extends Offer {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id")
    private Trader trader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    public ActiveOffer(Trader trader, Stock stock, BigDecimal price, int amount) {
        super(price, amount);
        this.trader = trader;
        this.stock = stock;
    }

    public OfferResponseDTO toResponseDTO() {
        OfferResponseDTO dto = new OfferResponseDTO();
        dto.setTraderId(this.getTrader().getId());
        dto.setStockId(this.getStock().getId());
        dto.setPrice(this.getPrice());
        dto.setAmount(this.getAmount());

        if (this instanceof BuyOffer) {
            dto.setType(OfferType.BUY);
        } else if (this instanceof SellOffer) {
            dto.setType(OfferType.SELL);
        }

        return dto;
    }


}

