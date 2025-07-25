package com.burse.bursebackend.dtos;

import com.burse.bursebackend.dtos.offer.OfferResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "Trader details including current balance and active offers.")
public class TraderDTO {

    @Schema(description = "Trader ID", example = "trader-123")
    private String id;

    @Schema(description = "Trader name", example = "Alice Levi")
    private String name;

    @Schema(description = "Trader's available balance", example = "5000.00")
    private BigDecimal money;

    @Schema(description = "Map of stock ID to quantity the trader owns",
            example = "{\"AAPL\": 10, \"GOOGL\": 5}")
    private Map<String, Integer> holdings;

    @Schema(description = "List of trader's open offers")
    private List<OfferResponseDTO> activeOffers;
}

