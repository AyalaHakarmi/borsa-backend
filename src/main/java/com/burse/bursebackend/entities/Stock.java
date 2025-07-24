package com.burse.bursebackend.entities;

import com.burse.bursebackend.entities.offer.ActiveTradeOffer;
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
    private List<ActiveTradeOffer> activeOffers = new ArrayList<>();

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trade> trades = new ArrayList<>();

    @PrePersist
    public void assignIdIfMissing() {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
    }
}


