package com.burse.bursebackend.dtos;

import com.burse.bursebackend.dtos.offer.ActiveOfferResponseDTO;
import com.burse.bursebackend.entities.Stock;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Detailed stock info including offers and recent trades.")
public class StockDetailDTO {

    @Schema(description = "Stock identifier", example = "AAPL")
    private String id;

    @Schema(description = "Company name", example = "Apple Inc.")
    private String name;

    @Schema(description = "Current market price", example = "194.35")
    private BigDecimal currentPrice;

    @Schema(description = "Total available amount", example = "1000")
    private int amount;

    @Schema(description = "List of active offers (buy/sell) for this stock")
    private List<ActiveOfferResponseDTO> offers;

    @Schema(description = "List of 10 most recent trades on this stock")
    private List<TradeDTO> recentTrades;

    public StockDetailDTO(Stock stock, List<ActiveOfferResponseDTO> offers, List<TradeDTO> recentTrades) {
        this.id = stock.getId();
        this.name = stock.getName();
        this.currentPrice = stock.getCurrentPrice();
        this.amount = stock.getAmount();
        this.offers = offers;
        this.recentTrades = recentTrades;
    }
}

