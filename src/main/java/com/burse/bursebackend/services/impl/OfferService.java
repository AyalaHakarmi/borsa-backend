package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.dtos.offer.*;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.*;
import com.burse.bursebackend.locks.RedisLockService;
import com.burse.bursebackend.types.ArchiveReason;
import com.burse.bursebackend.types.OfferType;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.types.ErrorCode;
import com.burse.bursebackend.repositories.offer.*;
import com.burse.bursebackend.services.IOfferService;
import com.burse.bursebackend.locks.LockKeyBuilder;
import com.burse.bursebackend.types.LockKeyType;
import com.burse.bursebackend.services.ITraderService;

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
    private final ITraderService traderService;
    private final OfferMapper offerMapper;

    @Override
    public ActiveOffer processNewOffer(BaseOfferDTO offerDTO) {
        Pair<ActiveOffer, OfferType> offer = offerMapper.buildOfferFromDTO(offerDTO);
        ActiveOffer newOffer = offer.getLeft();
        OfferType thisType = offer.getRight();
        String traderId = newOffer.getTrader().getId();
        String stockId = newOffer.getStock().getId();

        String oppositeTypeKey = LockKeyBuilder.buildKey(LockKeyType.OFFER_TYPE, traderId, stockId, thisType.opposite());
        String thisTypeKey = LockKeyBuilder.buildKey(LockKeyType.OFFER_TYPE, traderId, stockId, thisType);
        String metaKey = LockKeyBuilder.buildKey(LockKeyType.META, traderId, stockId);

        log.info("Received {} offer for trader {} on stock {} ",
            thisType.name(), newOffer.getTrader().getId(), newOffer.getStock().getId());

        redisLockService.lockMeta(metaKey);
        validateTypeBeforeInsertion(newOffer, thisTypeKey, metaKey);
        activeOfferRepository.save(newOffer);
        redisLockService.lock(oppositeTypeKey);

        newOffer.getTrader().addOffer(newOffer);
        newOffer.getStock().addOffer(newOffer);
        redisLockService.unlockMeta(metaKey);
        return newOffer;
    }

    private void validateTypeBeforeInsertion(ActiveOffer newOffer, String lockThisKey, String metaKey) {
        if (redisLockService.isLocked(lockThisKey)) {
            log.warn("Trader {} already has opposite offer type on stock {} – blocking new offer.",
                    newOffer.getTrader().getId(), newOffer.getStock().getId());
            redisLockService.unlockMeta(metaKey);
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
        unlockIfNoOtherOffersOfSameTypeExist(traderId, stockId, type);
    }

    private void unlockIfNoOtherOffersOfSameTypeExist(String traderId, String stockId, OfferType type) {
        String metaKey = LockKeyBuilder.buildKey(LockKeyType.META, traderId, stockId);
        redisLockService.lockMeta(metaKey);
        if (!isOfferOfTypeExists(type, traderId, stockId)) {
            String oppositeTypeKey = LockKeyBuilder.buildKey(LockKeyType.OFFER_TYPE, traderId, stockId, type.opposite());
            redisLockService.unlock(oppositeTypeKey);
        }
        redisLockService.unlock(metaKey);
    }

    private boolean isOfferOfTypeExists(OfferType type, String traderId, String stockId) {
        Optional<Trader> traderOpt = traderService.findById(traderId);
        if (traderOpt.isEmpty()) {
            log.warn("Trader {} not found while trying to release lock for offers of type {}", traderId, type);
            throw new BurseException(ErrorCode.TRADER_NOT_FOUND, "Trader not found while trying to release lock for offers of type " + type);
        }
        Trader trader = traderOpt.get();
        switch (type) {
            case BUY -> {
                return trader.getActiveOffers().stream()
                        .anyMatch(o -> o.getStock().getId().equals(stockId) && o instanceof BuyOffer);
            }
            case SELL -> {
                return trader.getActiveOffers().stream()
                        .anyMatch(o -> o.getStock().getId().equals(stockId) && o instanceof SellOffer);
            }
        };
        throw new BurseException(ErrorCode.UNKNOWN, "Unknown offer type");
    }

    @Transactional
    @Override
    public void cancelOffer(String offerId, ArchiveReason reason) {
        String lockKey = LockKeyBuilder.buildKey(LockKeyType.OFFER, offerId);
        if (reason != ArchiveReason.NO_FUNDS_AUTO_CANCEL && redisLockService.failLock(lockKey)) {
            log.warn("Failed to cancel offer {} – offer is locked, probably for processing a trade", offerId);
            throw new BurseException(ErrorCode.OFFER_LOCKED, "Offer is currently locked (probably for processing a trade). try again later.");
        }
        redisLockService.lock(lockKey);
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

    public List<ActiveOffer> getActiveOffersForStock(String stockId) {
        return activeOfferRepository.findByStockId(stockId);
    }

    @Override
    public List<ActiveOffer> getActiveOffersForTrader(String traderId) {
        return activeOfferRepository.findByTraderId(traderId);
    }

}

