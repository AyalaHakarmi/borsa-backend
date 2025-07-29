package com.burse.bursebackend.entities;

import com.burse.bursebackend.entities.offer.ActiveOffer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trade")
@Getter
@Setter
@NoArgsConstructor
public class Trade {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private Trader buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Trader seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(nullable = false)
    private BigDecimal pricePerUnit;

    private int amount;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    private LocalDateTime timestamp;

    @PrePersist
    public void assignIdAndTimestamp() {
        if (id == null || id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        if (timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    public Trade(ActiveOffer buyOffer, ActiveOffer sellOffer, BigDecimal pricePerUnit, int numOfStocksTraded) {
        this.buyer = buyOffer.getTrader();
        this.seller = sellOffer.getTrader();
        this.stock = buyOffer.getStock();
        this.pricePerUnit = pricePerUnit;
        this.amount = numOfStocksTraded;
        this.totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(numOfStocksTraded));
    }


}

