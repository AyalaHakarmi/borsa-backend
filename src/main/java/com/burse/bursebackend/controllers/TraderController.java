package com.burse.bursebackend.controllers;

import com.burse.bursebackend.dtos.TradeDTO;
import com.burse.bursebackend.dtos.TraderDTO;
import com.burse.bursebackend.services.ITraderService;
import com.burse.bursebackend.services.impl.BurseViewService;
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
    private final BurseViewService burseViewService;

    @GetMapping("/names")
    @Operation(summary = "Get all trader names", description = "Returns a list of all trader names.")
    public ResponseEntity<List<String>> getAllTraderNames() {
        List<String> traderNames = traderService.getAllTraderNames();
        return ResponseEntity.ok(traderNames);
    }

    @GetMapping("/{traderId}")
    @Operation(summary = "Get trader info with active offers", description = "Returns trader data and all his open offers.")
    public ResponseEntity<TraderDTO> getTraderDetails(@PathVariable String traderId) {
        TraderDTO traderDetails = burseViewService.getTraderDetails(traderId);
        return ResponseEntity.ok(traderDetails);
    }

    @GetMapping("/{traderId}/trades")
    @Operation(summary = "Get recent trades for trader", description = "Returns up to 8 recent trades involving the trader.")
    public ResponseEntity<List<TradeDTO>> getTraderTrades(@PathVariable String traderId) {
        List<TradeDTO> trades = burseViewService.get8RecentTradesForTrader(traderId);
        return ResponseEntity.ok(trades);
    }
}

