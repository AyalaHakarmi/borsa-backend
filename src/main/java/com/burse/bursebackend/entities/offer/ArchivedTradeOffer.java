package com.burse.bursebackend.entities.offer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "archived_trade_offer")
@NoArgsConstructor
@Getter
@Setter
public class ArchivedTradeOffer extends TradeOffer {

    @Enumerated(EnumType.STRING)
    private ArchiveReason reason;

    private LocalDateTime archivedAt;

    public ArchivedTradeOffer(TradeOffer originalOffer, ArchiveReason reason) {
        super(originalOffer.getTrader(), originalOffer.getStock(), originalOffer.getPrice(), originalOffer.getAmount());
        this.reason = reason;
    }



}

