package com.burse.bursebackend.entities;

import com.burse.bursebackend.dtos.StockDetailDTO;
import com.burse.bursebackend.dtos.stock.StockSimpleDTO;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock")
@Getter
@Setter
public class Stock {

    @Id
    private String id;

    private String name;

    private BigDecimal currentPrice;

    private int amount;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActiveOffer> activeOffers = new ArrayList<>();

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trade> trades = new ArrayList<>();

    @Version
    private int version;

    @PrePersist
    public void assignIdIfMissing() {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
    }


    public void addOffer(ActiveOffer newOffer) {
        if (newOffer != null && !activeOffers.contains(newOffer)) {
            activeOffers.add(newOffer);
            newOffer.setStock(this);
        }
    }

    public void addTrade(Trade trade) {
        if (trade != null && !trades.contains(trade)) {
            trades.add(trade);
            trade.setStock(this);
        }
    }

    public void removeOffer(ActiveOffer offer) {
        if (offer != null && activeOffers.contains(offer)) {
            activeOffers.remove(offer);
            offer.setStock(null);
        }
    }

    public StockSimpleDTO toStockSimpleDTO() {
        StockSimpleDTO dto = new StockSimpleDTO();
        dto.setId(this.getId());
        dto.setName(this.getName());
        dto.setCurrentPrice(this.getCurrentPrice());
        dto.setAmount(this.getAmount());
        return dto;
    }

    public StockDetailDTO toStockDetailDTO() {
        StockDetailDTO dto = new StockDetailDTO();
        dto.setId(this.getId());
        dto.setName(this.getName());
        dto.setCurrentPrice(this.getCurrentPrice());
        dto.setAmount(this.getAmount());
        return dto;
    }
}


