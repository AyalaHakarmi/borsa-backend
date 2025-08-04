package com.burse.bursebackend.dtos.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(description = "Basic stock data for overview list.")
public class StockSimpleDTO {

    @Schema(description = "Stock identifier", example = "AAPL")
    private String id;

    @Schema(description = "Company name", example = "Apple Inc.")
    private String name;

    @Schema(description = "Current market price", example = "194.35")
    private BigDecimal currentPrice;

    @Schema(description = "Total available amount", example = "1000")
    private int amount;

    public StockSimpleDTO(String id, String name, BigDecimal currentPrice, int amount) {
        this.id = id;
        this.name = name;
        this.currentPrice = currentPrice;
        this.amount = amount;
    }
}

