package com.burse.bursebackend.dtos.stock;

import com.burse.bursebackend.entities.Stock;
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

    public StockSimpleDTO(Stock stock) {
        this.id = stock.getId();
        this.name = stock.getName();
        this.currentPrice = stock.getCurrentPrice();
        this.amount = stock.getAmount();
    }
}

