package com.burse.bursebackend.dtos;

import com.burse.bursebackend.dtos.offer.OfferResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
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
    private List<OfferResponseDTO> offers;

    @Schema(description = "List of 10 most recent trades on this stock")
    private List<TradeDTO> recentTrades;
}

