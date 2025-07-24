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
@DiscriminatorValue("BUY")
@NoArgsConstructor
@Getter
@Setter
public class BuyOffer extends ActiveOffer {

    public BuyOffer(Trader trader, Stock stock, BigDecimal price, int amount) {
        super(trader, stock, price, amount);
    }

}

