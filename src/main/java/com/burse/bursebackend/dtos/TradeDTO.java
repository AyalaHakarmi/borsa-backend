package com.burse.bursebackend.dtos;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trade;
import com.burse.bursebackend.entities.Trader;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Schema(description = "Details of a trade executed on the platform.")
public class TradeDTO {

    @Schema(description = "Trade ID", example = "b1e4f98c-8aef-4d2a-9a53-9b3fdb775e22")
    private String id;

    @Schema(description = "ID of the stock being traded", example = "AAPL")
    private String stockId;

    @Schema(description = "Name of the stock", example = "Apple Inc.")
    private String stockName;

    @Schema(description = "Price per unit that was agreed upon", example = "187.75")
    private BigDecimal pricePerUnit;

    @Schema(description = "Total price of the trade", example = "9387.50")
    private BigDecimal totalPrice;

    @Schema(description = "Amount of stocks traded", example = "50")
    private int amount;

    @Schema(description = "Timestamp when the trade was executed")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;



    @Schema(description = "ID of the buyer", example = "buyer-uuid")
    private String buyerId;

    @Schema(description = "Name of the buyer", example = "Alice")
    private String buyerName;



    @Schema(description = "ID of the seller", example = "seller-uuid")
    private String sellerId;

    @Schema(description = "Name of the seller", example = "Bob")
    private String sellerName;



    public TradeDTO(Trade trade) {
        this.id = trade.getId();
        this.stockId = trade.getStock().getId();
        this.stockName = trade.getStock().getName();
        this.pricePerUnit = trade.getPricePerUnit();
        this.totalPrice = trade.getTotalPrice();
        this.amount = trade.getAmount();
        this.timestamp = trade.getTimestamp();

        this.buyerId = trade.getBuyer().getId();
        this.buyerName = trade.getBuyer().getName();

        this.sellerId = trade.getSeller().getId();
        this.sellerName = trade.getSeller().getName();
    }
}

