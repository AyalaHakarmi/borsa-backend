package com.burse.bursebackend.dtos.offer;

import com.burse.bursebackend.types.OfferType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Represents an active buy or sell offer.")
public class ActiveOfferResponseDTO extends BaseOfferDTO {

    @Schema(description = "Unique identifier for the offer", example = "12345")
    private String id;

    @Schema(description = "Type of the offer: BUY or SELL", example = "BUY")
    private OfferType type;

    @Schema(description = "Timestamp when the offer was created", example = "2021-01-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Name of the stock the offer applies to", example = "APPLE")
    private String stockName;

    public ActiveOfferResponseDTO(String traderId, String stockId, BigDecimal price, int amount, String offerId, OfferType offerType, LocalDateTime createdAt, String stockName) {
        super(traderId, stockId, price, amount);
        this.id = offerId;
        this.type = offerType;
        this.createdAt = createdAt;
        this.stockName = stockName;
    }
}

