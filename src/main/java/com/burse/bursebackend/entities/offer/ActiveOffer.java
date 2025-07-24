package com.burse.bursebackend.entities.offer;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
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
public abstract class ActiveOffer extends Offer {
    public ActiveOffer(Trader trader, Stock stock, BigDecimal price, int amount) {
        super(trader, stock, price, amount);
    }


}

