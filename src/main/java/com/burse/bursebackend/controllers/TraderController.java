package com.burse.bursebackend.controllers;

import com.burse.bursebackend.dtos.TradeDTO;
import com.burse.bursebackend.services.ITradeService;
import com.burse.bursebackend.services.ITraderService;
import com.burse.bursebackend.services.impl.DataAggregatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traders")
@RequiredArgsConstructor
@Tag(name = "Traders", description = "Endpoints for retrieving trader information.")
public class TraderController {

    private final ITraderService traderService;
    private final DataAggregatorService dataAggregatorService;
    private final ITradeService tradeService;

    @GetMapping("/names")
    @Operation(summary = "Get all trader names", description = "Returns a list of all trader names.")
    public ResponseEntity<List<String>> getAllTraderNames() {
        return ResponseEntity.ok(traderService.getAllTraderNames());
    }

    @GetMapping("/{traderId}")
    @Operation(summary = "Get trader info with active offers", description = "Returns trader data and all their open offers.")
    public ResponseEntity<?> getTraderDetails(@PathVariable String traderId) {
        return ResponseEntity.ok(dataAggregatorService.getTraderDetails(traderId));
    }

    @GetMapping("/{traderId}/trades")
    @Operation(summary = "Get recent trades for trader", description = "Returns up to 8 recent trades involving the trader.")
    public ResponseEntity<List<TradeDTO>> getTraderTrades(@PathVariable String traderId) {
        return ResponseEntity.ok(tradeService.getRecentTradesForTrader(traderId));
    }
}

