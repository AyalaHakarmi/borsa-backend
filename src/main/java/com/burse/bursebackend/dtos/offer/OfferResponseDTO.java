package com.burse.bursebackend.dtos.offer;

import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.enums.OfferType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Represents a buy or sell offer for a specific stock.")
public class OfferResponseDTO extends BaseOfferDTO {

    @Schema(description = "Type of the offer: BUY or SELL", example = "BUY")
    private OfferType type;


}

