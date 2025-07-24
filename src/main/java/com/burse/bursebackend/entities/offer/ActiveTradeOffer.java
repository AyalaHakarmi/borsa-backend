package com.burse.bursebackend.entities.offer;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "offer_type")
@Table(name = "active_offers")
@Getter
@Setter
public abstract class ActiveTradeOffer extends TradeOffer {
    public ActiveTradeOffer(Trader trader, Stock stock, BigDecimal price, int amount) {
        super(trader, stock, price, amount);
    }


}

