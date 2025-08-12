package com.burse.bursebackend.services.interfaces.offer;

import com.burse.bursebackend.dtos.offer.BaseOfferDTO;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.types.ArchiveReason;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IOfferService {
    ActiveOffer processNewOffer(BaseOfferDTO offerDTO);

    boolean offersExist(ActiveOffer... offers);

    void reduceOfferAmount(ActiveOffer offer, int quantityToReduce);

    void archiveOffer(ActiveOffer offer, ArchiveReason archiveReason);

    @Transactional
    void cancelOffer(String offerId, ArchiveReason reason);

    List<ActiveOffer> getActiveOffersForStock(String stockId);

    List<ActiveOffer> getActiveOffersForTrader(String traderId);

    Pair<BuyOffer, SellOffer> findMatchingOffer(ActiveOffer newOffer);
}

