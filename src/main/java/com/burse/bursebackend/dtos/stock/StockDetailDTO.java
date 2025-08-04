package com.burse.bursebackend.dtos.stock;

import com.burse.bursebackend.dtos.TradeDTO;
import com.burse.bursebackend.dtos.offer.ActiveOfferResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Schema(description = "Detailed stock info including offers and recent trades.")
public class StockDetailDTO extends StockSimpleDTO {

    @Schema(description = "List of active offers (buy/sell) for this stock")
    private List<ActiveOfferResponseDTO> offers;

    @Schema(description = "List of 10 most recent trades on this stock")
    private List<TradeDTO> recentTrades;


    public StockDetailDTO(String id, String name, BigDecimal currentPrice, int amount, List<ActiveOfferResponseDTO> activeOffers, List<TradeDTO> trades) {
        super(id, name, currentPrice, amount);
        this.offers = activeOffers;
        this.recentTrades = trades;
    }
}

