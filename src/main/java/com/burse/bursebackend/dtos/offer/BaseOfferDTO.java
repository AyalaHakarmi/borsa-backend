package com.burse.bursebackend.dtos.offer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Base class for all offer types (buy/sell). Contains common fields.")
public abstract class BaseOfferDTO {

    @NotBlank
    @Schema(description = "Trader ID who placed the offer", example = "trader-123")
    private String traderId;

    @NotBlank
    @Schema(description = "Stock ID the offer applies to", example = "stock-ABC")
    private String stockId;

    @Schema(description = "Price per unit for the offer", example = "100.50")
    private BigDecimal price;

    @Min(1)
    @Schema(description = "Number of stocks", example = "10")
    private int amount;
}
