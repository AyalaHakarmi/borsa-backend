package com.burse.bursebackend.controllers;

import com.burse.bursebackend.pricing.IStockPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/strategy")
@Tag(name = "Strategy Management", description = "Switch and query price update strategies")
public class StrategyController {

    private final IStockPriceService stockPriceUpdater;

    @GetMapping
    @Operation(summary = "Get current strategy", description = "Returns the currently active stock price update strategy")
    public ResponseEntity<String> getCurrentStrategy() {
        String currentStrategy = stockPriceUpdater.getCurrentStrategy();
        return ResponseEntity.ok(currentStrategy);
    }

    @GetMapping("/available-strategies")
    @Operation(summary = "List all strategies", description = "Returns all available strategy names that can be activated")
    public ResponseEntity<Map<String,String>> getAvailableStrategies() {
        Map<String,String> availableStrategies = stockPriceUpdater.getAvailableStrategies();
        return ResponseEntity.ok(availableStrategies);
    }

    @PutMapping("/switch")
    @Operation(summary = "Switch strategy", description = "Switches the current strategy to the given name (e.g. pureRandomStrategy)")
    public ResponseEntity<String> switchStrategy(@RequestParam String strategyName) {
        String result = stockPriceUpdater.switchStrategy(strategyName);
        return ResponseEntity.ok(result);
    }
}

