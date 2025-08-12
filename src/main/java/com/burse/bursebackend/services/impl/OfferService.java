package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.dtos.offer.*;
import com.burse.bursebackend.entities.offer.*;
import com.burse.bursebackend.redis.RedisCounterService;
import com.burse.bursebackend.redis.RedisLockService;
import com.burse.bursebackend.types.ArchiveReason;
import com.burse.bursebackend.types.OfferType;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.types.ErrorCode;
import com.burse.bursebackend.repositories.offer.*;
import com.burse.bursebackend.services.IOfferService;
import com.burse.bursebackend.redis.KeyBuilder;
import com.burse.bursebackend.types.KeyType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService implements IOfferService {

    private final ActiveOfferRepository activeOfferRepository;
    private final SellOfferRepository sellOfferRepository;
    private final BuyOfferRepository buyOfferRepository;
    private final ArchivedOfferRepository archivedOfferRepository;
    private final RedisLockService redisLockService;
    private final RedisCounterService redisCounterService;
    private final OfferMapper offerMapper;

    @Override
    public ActiveOffer processNewOffer(BaseOfferDTO offerDTO) {
        Pair<ActiveOffer, OfferType> offer = offerMapper.buildOfferFromDTO(offerDTO);
        ActiveOffer newOffer = offer.getLeft();
        OfferType thisType = offer.getRight();
        String traderId = newOffer.getTrader().getId();
        String stockId = newOffer.getStock().getId();

        log.info("Received {} offer for trader {} on stock {} ",
            thisType.name(), newOffer.getTrader().getId(), newOffer.getStock().getId());

        validateTypeBeforeInsertion(traderId, stockId, thisType);
        try{
            activeOfferRepository.save(newOffer);
        }catch (Exception e){
            redisCounterService.removeOffer(traderId, stockId, thisType);
        }
        newOffer.getTrader().addOffer(newOffer);
        newOffer.getStock().addOffer(newOffer);
        return newOffer;
    }

    private void validateTypeBeforeInsertion(String traderId, String stockId, OfferType thisType) {
        if (!redisCounterService.tryAddOffer(traderId, stockId, thisType)){
            log.warn("Trader {} already has opposite offer type on stock {} – blocking new offer.",
                    traderId, stockId);
            throw new BurseException(
                    ErrorCode.INVALID_OFFER,
                    "You already have an offer of the opposite type for this stock."
            );
        }
    }

    @Override
    public Pair<BuyOffer, SellOffer> findMatchingOffer(ActiveOffer newOffer) {
        String stockId = newOffer.getStock().getId();
        if (newOffer instanceof BuyOffer) {
            List<SellOffer> matches = sellOfferRepository.findBestMatchingSellOffer(
                    stockId,
                    newOffer.getPrice(),
                    PageRequest.of(0, 1)
            );
            SellOffer matching = matches.isEmpty() ? null : matches.get(0);
            return Pair.of((BuyOffer) newOffer, matching);

        } else if (newOffer instanceof SellOffer) {
            List<BuyOffer> matches = buyOfferRepository.findBestMatchingBuyOffer(
                    stockId,
                    newOffer.getPrice(),
                    PageRequest.of(0, 1)
            );

            BuyOffer matching = matches.isEmpty() ? null : matches.get(0);
            return Pair.of(matching, (SellOffer) newOffer);
        }
        throw new BurseException(ErrorCode.INVALID_OFFER, "Unknown offer type");
    }

    @Override
    public boolean offersExist(ActiveOffer... offers) {
        if (offers != null) {
            for (ActiveOffer offer : offers) {
                if(!activeOfferRepository.existsById(offer.getId())) {
                    log.debug("Offer {} no longer exists", offer.getId());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void reduceOfferAmount(ActiveOffer offer, int quantityToReduce) {
        int remaining = offer.getAmount()-quantityToReduce;
        log.info("Updating offer {}. Reduced amount by {}, remaining: {}",
                offer.getId(), quantityToReduce, remaining);
        offer.setAmount(remaining);
        activeOfferRepository.save(offer);
    }

    @Transactional
    @Override
    public void archiveOffer(ActiveOffer offer, ArchiveReason archiveReason) {
        String traderId = offer.getTrader().getId();
        String stockId = offer.getStock().getId();
        OfferType type = offer instanceof BuyOffer ? OfferType.BUY : OfferType.SELL;
        ArchivedOffer archived = new ArchivedOffer(offer, archiveReason);
        archivedOfferRepository.save(archived);

        log.info("Archiving offer {} for trader {} on stock {} (type: {}) due to {}",
                offer.getId(), traderId, stockId, type, archiveReason.name());

        offer.getTrader().removeOffer(offer);
        offer.getStock().removeOffer(offer);
        activeOfferRepository.deleteById(offer.getId());
        activeOfferRepository.flush();
        redisCounterService.removeOffer(traderId, stockId, type);
    }

    @Transactional
    @Override
    public void cancelOffer(String offerId, ArchiveReason reason) {
        String lockKey = KeyBuilder.buildKey(KeyType.OFFER, offerId);
        if (reason != ArchiveReason.NO_FUNDS_AUTO_CANCEL && !redisLockService.tryAcquireLock(lockKey)) {
            log.warn("Failed to cancel offer {} – offer is locked, probably for processing a trade", offerId);
            throw new BurseException(ErrorCode.OFFER_LOCKED, "Offer is currently locked (probably for processing a trade). try again later.");
        }
        try {
            Optional<ActiveOffer> offer = activeOfferRepository.findById(offerId);
            if (offer.isEmpty()) {
                log.warn("Offer {} not found for cancellation", offerId);
                throw new BurseException(ErrorCode.OFFER_NOT_FOUND, "Offer not found for cancellation");
            }
            archiveOffer(offer.get(), reason);
        } finally {
            redisLockService.unlock(lockKey);
        }
    }

    @Override
    public List<ActiveOffer> getActiveOffersForStock(String stockId) {
        return activeOfferRepository.findByStockId(stockId);
    }

    @Override
    public List<ActiveOffer> getActiveOffersForTrader(String traderId) {
        return activeOfferRepository.findByTraderId(traderId);
    }

}

