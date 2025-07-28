package com.burse.bursebackend.dtos.offer;


import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.types.OfferType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Represents an active buy or sell offer.")
public class ActiveOfferResponseDTO extends BaseOfferDTO {

    @Schema(description = "Unique identifier for the offer", example = "12345")
    private String id;

    @Schema(description = "Type of the offer: BUY or SELL", example = "BUY")
    private OfferType type;


    public ActiveOfferResponseDTO(ActiveOffer activeOffer) {
        super(activeOffer);
        this.id = activeOffer.getId();
        this.type = activeOffer instanceof com.burse.bursebackend.entities.offer.BuyOffer ? OfferType.BUY : OfferType.SELL;
    }
}

