package com.burse.bursebackend.entities.offer;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@NoArgsConstructor
@Getter
@Setter
public abstract class Offer {

    @Id
    @Column(unique = true)
    private String id;

    private BigDecimal price;

    private int amount;

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
        if (this instanceof ArchivedOffer archived) {
            archived.setArchivedAt(LocalDateTime.now());
        }
    }

    public Offer(BigDecimal price, int amount) {
        this.price = price;
        this.amount = amount;
    }

}


