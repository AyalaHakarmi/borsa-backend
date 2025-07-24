package com.burse.bursebackend.controllers;

import com.burse.bursebackend.services.pricing.StockPriceUpdater;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/strategy")
@Tag(name = "Strategy Management", description = "Switch and query price update strategies")
public class StrategyController {

    private final StockPriceUpdater stockPriceUpdater;

    public StrategyController(StockPriceUpdater stockPriceUpdater) {
        this.stockPriceUpdater = stockPriceUpdater;
    }

    @GetMapping("/current")
    @Operation(summary = "Get current strategy", description = "Returns the currently active stock price update strategy")
    public String getCurrentStrategy() {
        return stockPriceUpdater.getCurrentStrategyName();
    }

    @GetMapping("/available")
    @Operation(summary = "List all strategies", description = "Returns all available strategy names that can be activated")
    public Set<String> getAvailableStrategies() {
        return stockPriceUpdater.getAvailableStrategyNames();
    }

    @PostMapping("/switch")
    @Operation(summary = "Switch strategy", description = "Switches the current strategy to the given name (e.g. pureRandomStrategy)")
    public ResponseEntity<String> switchStrategy(@RequestParam String name) {
        try {
            stockPriceUpdater.setStrategyByName(name);
            return ResponseEntity.ok("Switched to strategy: " + name);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Unknown strategy: " + name);
        }
    }
}

