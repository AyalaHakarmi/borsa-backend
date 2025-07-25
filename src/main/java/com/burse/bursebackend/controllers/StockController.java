package com.burse.bursebackend.controllers;

import com.burse.bursebackend.dtos.StockDetailDTO;
import com.burse.bursebackend.dtos.stock.StockSimpleDTO;
import com.burse.bursebackend.services.impl.DataAggregatorService;
import com.burse.bursebackend.services.stocks.IStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Tag(name = "Stocks", description = "Endpoints for retrieving stock information.")
public class StockController {

    private final IStockService stockService;
    private final DataAggregatorService dataAggregatorService;


    @GetMapping
    @Operation(summary = "Get all stocks", description = "Returns a list of all stocks with basic information.")
    public ResponseEntity<List<StockSimpleDTO>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/{stockId}")
    @Operation(summary = "Get detailed stock info", description = "Returns detailed stock info including offers and 10 latest trades.")
    public ResponseEntity<StockDetailDTO> getStockDetails(@PathVariable String stockId) {
        return ResponseEntity.ok(dataAggregatorService.getStockDetails(stockId));
    }
}
