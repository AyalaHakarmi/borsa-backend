package com.burse.bursebackend.entities;

import com.burse.bursebackend.dtos.TradeDTO;
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

    private BigDecimal pricePerUnit;

    private int amount;

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

    public Trade(ActiveOffer buyOffer, ActiveOffer sellOffer, BigDecimal pricePerUnit, int tradeQty) {
        this.buyer = buyOffer.getTrader();
        this.seller = sellOffer.getTrader();
        this.stock = buyOffer.getStock();
        this.pricePerUnit = pricePerUnit;
        this.amount = tradeQty;
        this.totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(tradeQty));
    }

    public TradeDTO toDTO() {
        TradeDTO dto = new TradeDTO();

        dto.setId(this.getId());
        dto.setPrice(this.getPricePerUnit());
        dto.setTotalPrice(this.getTotalPrice());
        dto.setAmount(this.getAmount());
        dto.setTimestamp(this.getTimestamp());

        dto.setStockId(this.getStock().getId());
        dto.setStockName(this.getStock().getName());

        dto.setBuyerId(this.getBuyer().getId());
        dto.setBuyerName(this.getBuyer().getName());

        dto.setSellerId(this.getSeller().getId());
        dto.setSellerName(this.getSeller().getName());

        return dto;
    }
}

