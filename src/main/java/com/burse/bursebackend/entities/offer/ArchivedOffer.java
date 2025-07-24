package com.burse.bursebackend.entities.offer;

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

    public ArchivedOffer(Offer originalOffer, ArchiveReason reason) {
        super(originalOffer.getTrader(), originalOffer.getStock(), originalOffer.getPrice(), originalOffer.getAmount());
        this.reason = reason;
        this.offerType= (
                originalOffer instanceof BuyOffer ? OfferType.BUY : OfferType.SELL
        );
    }



}

