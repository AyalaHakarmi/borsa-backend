package com.burse.bursebackend.dtos;

import com.burse.bursebackend.dtos.offer.ActiveOfferResponseDTO;
import com.burse.bursebackend.entities.Trader;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Schema(description = "Trader details including current balance and active offers.")
public class TraderDTO {

    @Schema(description = "Trader ID", example = "trader-123")
    private String id;

    @Schema(description = "Trader name", example = "Alice Levi")
    private String name;

    @Schema(description = "Trader's available balance", example = "5000.00")
    private BigDecimal money;

    @Schema(description = "Map of stock ID to quantity the trader owns",
            example = "{\"APPLE\": 10, \"GOOGLE\": 5}")
    private Map<String, Integer> holdings;

    @Schema(description = "List of trader's open offers")
    private List<ActiveOfferResponseDTO> activeOffers;


    public TraderDTO(Trader trader, List<ActiveOfferResponseDTO> activeOffersForTrader) {
        this.id = trader.getId();
        this.name = trader.getName();
        this.money = trader.getMoney();
        this.holdings = trader.getHoldings();
        this.activeOffers = activeOffersForTrader;
    }
}

