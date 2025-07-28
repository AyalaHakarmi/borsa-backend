package com.burse.bursebackend.entities;

import com.burse.bursebackend.entities.offer.ActiveOffer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "trader")
@Getter
@Setter
public class Trader {

    @Id
    private String id;

    private String name;

    @Column(nullable = false)
    private BigDecimal money;

    @ElementCollection
    @CollectionTable(name = "trader_holdings", joinColumns = @JoinColumn(name = "trader_id"))
    @MapKeyColumn(name = "stock_id")
    @Column(name = "amount")
    private Map<String, Integer> holdings = new HashMap<>();

    @OneToMany(mappedBy = "trader", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActiveOffer> activeOffers = new ArrayList<>();

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    private List<Trade> asBuyer = new ArrayList<>();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Trade> asSeller = new ArrayList<>();

    @Transient
    public List<Trade> getAllTrades() {
        List<Trade> all = new ArrayList<>();
        all.addAll(asBuyer);
        all.addAll(asSeller);
        all.sort(Comparator.comparing(Trade::getTimestamp).reversed());
        return all;
    }

    @PrePersist
    public void assignIdIfMissing() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }

    public void addOffer(ActiveOffer newOffer) {
        if (newOffer != null) {
            newOffer.setTrader(this);
            activeOffers.add(newOffer);
        }
    }

    public void addAsBuyerTrade(Trade trade) {
        if (trade != null) {
            trade.setBuyer(this);
            asBuyer.add(trade);
        }
    }

    public void addAsSellerTrade(Trade trade) {
        if (trade != null) {
            trade.setSeller(this);
            asSeller.add(trade);
        }
    }

    public void removeOffer(ActiveOffer offer) {
        if (offer != null) {
            activeOffers.remove(offer);
            offer.setTrader(null);
        }
    }

}

