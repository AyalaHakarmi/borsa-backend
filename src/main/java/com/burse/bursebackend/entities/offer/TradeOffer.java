package com.burse.bursebackend.entities.offer;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@NoArgsConstructor
@Getter
@Setter
public abstract class TradeOffer {

    @Id
    private String id;

    private BigDecimal price;

    private int amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id")
    private Trader trader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    private LocalDateTime createdAt;

    @Version
    private int version;

    @PrePersist
    public void assignIdAndTimestamp() {
        if (id == null || id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this instanceof ArchivedTradeOffer archived) {
            archived.setArchivedAt(LocalDateTime.now());
        }
    }

    public TradeOffer(Trader trader, Stock stock, BigDecimal price, int amount) {
        this.trader = trader;
        this.stock = stock;
        this.price = price;
        this.amount = amount;
    }

}


