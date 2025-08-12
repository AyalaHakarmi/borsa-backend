package com.burse.bursebackend.controllers;

import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.burse.bursebackend.dtos.offer.SellOfferDTO;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.services.interfaces.offer.IOfferService;
import com.burse.bursebackend.services.interfaces.trade.ITradeService;
import com.burse.bursebackend.types.ArchiveReason;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Tag(name = "Offers", description = "Endpoints for placing and canceling buy/sell offers.")
public class OfferController {

    private final IOfferService offerService;
    private final ITradeService tradeService;

    @PostMapping("/buy")
    @Operation(summary = "Place a new buy offer", description = "Creates a new buy offer based on the provided DTO.")
    public ResponseEntity<Void> placeBuyOffer(@RequestBody @Valid BuyOfferDTO buyOfferDTO) {
        ActiveOffer activeOffer = offerService.processNewOffer(buyOfferDTO);
        tradeService.searchPotentialTrade(activeOffer);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sell")
    @Operation(summary = "Place a new sell offer", description = "Creates a new sell offer based on the provided DTO.")
    public ResponseEntity<Void> placeSellOffer(@RequestBody @Valid SellOfferDTO sellOfferDTO) {
        ActiveOffer activeOffer = offerService.processNewOffer(sellOfferDTO);
        tradeService.searchPotentialTrade(activeOffer);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/buy/cancel/{offerId}")
    @Operation(summary = "Cancel a buy offer", description = "Cancels a buy offer using the given ID.")
    public ResponseEntity<Void> cancelBuyOffer(@PathVariable String offerId) {
        offerService.cancelOffer(offerId, ArchiveReason.CANCELED);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sell/cancel/{offerId}")
    @Operation(summary = "Cancel a sell offer", description = "Cancels a sell offer using the given ID.")
    public ResponseEntity<Void> cancelSellOffer(@PathVariable String offerId) {
        offerService.cancelOffer(offerId, ArchiveReason.CANCELED);
        return ResponseEntity.noContent().build();
    }
}
