package com.burse.bursebackend.services;

import com.burse.bursebackend.dtos.offer.BaseOfferDTO;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.entities.offer.ArchiveReason;
import com.burse.bursebackend.exceptions.BurseException;

public interface IOfferService {
    void placeOffer(BaseOfferDTO offerDTO) throws BurseException;

    void tryFindPotentialTrade(ActiveOffer newOffer);

    void archiveOffer(ActiveOffer offer, ArchiveReason archiveReason);

    void cancelOffer(String offerId);
}

