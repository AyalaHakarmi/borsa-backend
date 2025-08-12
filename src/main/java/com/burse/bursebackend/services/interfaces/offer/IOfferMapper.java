package com.burse.bursebackend.services.interfaces.offer;

import com.burse.bursebackend.dtos.offer.BaseOfferDTO;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.types.OfferType;
import org.apache.commons.lang3.tuple.Pair;

public interface IOfferMapper {
    Pair<ActiveOffer, OfferType> buildOfferFromDTO(BaseOfferDTO offerDTO);
}
