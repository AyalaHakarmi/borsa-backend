package com.burse.bursebackend.dtos.stock;

import com.burse.bursebackend.dtos.TradeDTO;
import com.burse.bursebackend.dtos.offer.ActiveOfferResponseDTO;
import com.burse.bursebackend.entities.Stock;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Detailed stock info including offers and recent trades.")
public class StockDetailDTO extends StockSimpleDTO {

    @Schema(description = "List of active offers (buy/sell) for this stock")
    private List<ActiveOfferResponseDTO> offers;

    @Schema(description = "List of 10 most recent trades on this stock")
    private List<TradeDTO> recentTrades;

    public StockDetailDTO(Stock stock, List<ActiveOfferResponseDTO> offers, List<TradeDTO> recentTrades) {
        super(stock);
        this.offers = offers;
        this.recentTrades = recentTrades;
    }
}

