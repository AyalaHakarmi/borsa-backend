package com.burse.bursebackend.dtos.offer;

import com.burse.bursebackend.entities.offer.ActiveOffer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Base class for all offer types (buy/sell). Contains common fields.")
public abstract class BaseOfferDTO {

    @NotBlank
    @Schema(description = "Trader ID who placed the offer", example = "13")
    private String traderId;

    @NotBlank
    @Schema(description = "Stock ID the offer applies to", example = "14")
    private String stockId;

    @Schema(description = "Price per unit for the offer", example = "100.50")
    private BigDecimal price;

    @Min(1)
    @Schema(description = "Number of stocks", example = "10")
    private int amount;


    public BaseOfferDTO(ActiveOffer activeOffer) {
        this.traderId = activeOffer.getTrader() != null ? activeOffer.getTrader().getId() : null;
        this.stockId = activeOffer.getStock() != null ? activeOffer.getStock().getId() : null;
        this.price = activeOffer.getPrice();
        this.amount = activeOffer.getAmount();
    }
}
