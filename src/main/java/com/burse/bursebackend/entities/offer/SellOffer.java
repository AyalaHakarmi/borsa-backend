package com.burse.bursebackend.entities.offer;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@DiscriminatorValue("SELL")
@NoArgsConstructor
@Getter
@Setter
public class SellOffer extends ActiveOffer {

    public SellOffer(Trader trader, Stock stock, BigDecimal price, int amount) {
        super(trader, stock, price, amount);
    }

}

