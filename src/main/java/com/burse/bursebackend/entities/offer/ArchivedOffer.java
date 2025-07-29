package com.burse.bursebackend.entities.offer;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.types.ArchiveReason;
import com.burse.bursebackend.types.OfferType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "archived_offer")
@NoArgsConstructor
@Getter
@Setter
public class ArchivedOffer extends Offer {

    @Enumerated(EnumType.STRING)
    private ArchiveReason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferType offerType;


    private LocalDateTime archivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id")
    private Trader trader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;


    public ArchivedOffer(ActiveOffer originalOffer, ArchiveReason reason) {
        super(originalOffer.getPrice(), originalOffer.getAmount());
        this.reason = reason;
        this.offerType= (
                originalOffer instanceof BuyOffer ? OfferType.BUY : OfferType.SELL
        );
        this.trader = originalOffer.getTrader();
        this.stock = originalOffer.getStock();
    }



}

