package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.dtos.offer.BaseOfferDTO;
import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.burse.bursebackend.dtos.offer.OfferResponseDTO;
import com.burse.bursebackend.dtos.offer.SellOfferDTO;
import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.*;
import com.burse.bursebackend.enums.ArchiveReason;
import com.burse.bursebackend.enums.OfferType;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.enums.ErrorCode;
import com.burse.bursebackend.repositories.offer.ActiveOfferRepository;
import com.burse.bursebackend.repositories.offer.ArchivedOfferRepository;
import com.burse.bursebackend.repositories.offer.BuyOfferRepository;
import com.burse.bursebackend.repositories.offer.SellOfferRepository;
import com.burse.bursebackend.services.IOfferService;
import com.burse.bursebackend.locks.IRedisLockService;
import com.burse.bursebackend.services.ITradeService;
import com.burse.bursebackend.locks.LockKeyBuilder;
import com.burse.bursebackend.enums.LockKeyType;
import com.burse.bursebackend.services.ITraderService;
import com.burse.bursebackend.services.stocks.IStockService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferService implements IOfferService {

    private final IRedisLockService redisLockService;
    private final ITradeService tradeService;
    private final ActiveOfferRepository activeOfferRepository;
    private final SellOfferRepository sellOfferRepository;
    private final BuyOfferRepository buyOfferRepository;
    private final ArchivedOfferRepository archivedOfferRepository;
    private final ITraderService traderService;
    private final IStockService stockService;

    @Override
    public void placeOffer(BaseOfferDTO offerDTO) {
        OfferType thisType = extractType(offerDTO);
        OfferType oppositeType = thisType.opposite();

        String traderId = offerDTO.getTraderId();
        String stockId = offerDTO.getStockId();


        String lockOppositeKey = LockKeyBuilder.buildKey(LockKeyType.OFFER_TYPE, traderId, stockId, oppositeType);
        String lockThisKey = LockKeyBuilder.buildKey(LockKeyType.OFFER_TYPE, traderId, stockId, thisType);
        String metaKey = LockKeyBuilder.buildKey(LockKeyType.META, traderId, stockId);

        redisLockService.lockMeta(metaKey);

        ActiveOffer newOffer;
        try {
            if (redisLockService.isLocked(lockThisKey)) {
                throw new BurseException(
                        ErrorCode.OPPOSITE_OFFER_EXISTS,
                        "You already have an offer of " + oppositeType.name() + " type for this stock."
                );
            }
            newOffer = fromDto(offerDTO);
            activeOfferRepository.save(newOffer);
            redisLockService.lock(lockOppositeKey);

        } finally {
            redisLockService.unlockMeta(metaKey);
        }
        newOffer.getTrader().addOffer(newOffer);
        newOffer.getStock().addOffer(newOffer);
        tryFindPotentialTrade(newOffer);
    }

    @Override
    public void tryFindPotentialTrade(ActiveOffer newOffer) {
        while (true) {
            Optional<ActiveOffer> matchOpt = findMatchingOffer(newOffer);
            if (matchOpt.isEmpty()) return;

            ActiveOffer matchedOffer = matchOpt.get();

            Pair<BuyOffer, SellOffer> pair = extractBuyAndSellOffers(newOffer, matchedOffer);
            BuyOffer buyOffer = pair.getLeft();
            SellOffer sellOffer = pair.getRight();


            String lockBuyOffer = LockKeyBuilder.buildKey(LockKeyType.OFFER, buyOffer.getId());

            if (redisLockService.failLock(lockBuyOffer)) continue;

            String lockSellOffer = LockKeyBuilder.buildKey(LockKeyType.OFFER, sellOffer.getId());

            if (redisLockService.failLock(lockSellOffer)) {
                redisLockService.unlock(lockBuyOffer);
                continue;
            }

            int tradeQty =Math.min(buyOffer.getAmount(), sellOffer.getAmount());

            try{
                tradeService.executeTrade(buyOffer, sellOffer, tradeQty);
            }
            catch (BurseException ex){
                redisLockService.unlock(lockBuyOffer, lockSellOffer);
                if (ex.getErrorCode() == ErrorCode.TRY_ANOTHER_MATCH) {
                    continue;
                }
                throw ex;
            }

            updateOrArchiveOffer(buyOffer, tradeQty, lockBuyOffer);
            updateOrArchiveOffer(sellOffer, tradeQty,lockSellOffer);

            break;
        }
    }

    private Pair<BuyOffer, SellOffer> extractBuyAndSellOffers(ActiveOffer newOffer, ActiveOffer matchedOffer) {
        if (newOffer instanceof BuyOffer buy) {
            return Pair.of(buy, (SellOffer) matchedOffer);
        } else {
            return Pair.of((BuyOffer) matchedOffer, (SellOffer) newOffer);
        }
    }

    private Optional<ActiveOffer> findMatchingOffer(ActiveOffer newOffer) {
        String stockId = newOffer.getStock().getId();

        if (newOffer instanceof BuyOffer buy) {
            return sellOfferRepository.findBestMatchingSellOffer(
                    stockId,
                    buy.getPrice())
                    .map(offer -> (ActiveOffer) offer);

        } else if (newOffer instanceof SellOffer sell) {
            return buyOfferRepository.findBestMatchingBuyOffer(
                    stockId,
                    sell.getPrice()
            ).map(offer -> (ActiveOffer) offer);
        }

        return Optional.empty();
    }

    public void updateOrArchiveOffer(ActiveOffer offer, int quantityToReduce, String offerLockKey) {
        int remaining = offer.getAmount()-quantityToReduce;
        offer.setAmount(remaining);
        activeOfferRepository.save(offer);
        if (remaining == 0) {
            try{
                archiveOffer(offer, ArchiveReason.COMPLETED);
            }finally {
                redisLockService.unlock(offerLockKey);
            }
        } else {
            redisLockService.unlock(offerLockKey);
            new Thread(() -> tryFindPotentialTrade(offer)).start();
        }
    }

    @Override
    public void archiveOffer(ActiveOffer offer, ArchiveReason archiveReason) {

        String traderId = offer.getTrader().getId();
        String stockId = offer.getStock().getId();
        OfferType type = offer instanceof BuyOffer ? OfferType.BUY : OfferType.SELL;

        ArchivedOffer archived = new ArchivedOffer(offer, archiveReason);
        archivedOfferRepository.save(archived);
        offer.getTrader().removeOffer(offer);
        offer.getStock().removeOffer(offer);
        switch (type) {
            case BUY -> buyOfferRepository.delete((BuyOffer) offer);
            case SELL -> sellOfferRepository.delete((SellOffer) offer);
            default -> throw new IllegalArgumentException("Unknown offer type: " + type);
        }
        releaseLockIfNoOtherOffersOfType(traderId, stockId, type);
    }

    private void releaseLockIfNoOtherOffersOfType(String traderId, String stockId, OfferType type) {
        String metaKey = LockKeyBuilder.buildKey(LockKeyType.META, traderId, stockId);

        redisLockService.lockMeta(metaKey);

        try {
            boolean hasMoreSameType = switch (type) {
                case BUY -> buyOfferRepository.existsByTraderIdAndStockId(traderId, stockId);
                case SELL -> sellOfferRepository.existsByTraderIdAndStockId(traderId, stockId);
            };

            if (!hasMoreSameType) {
                String lockOppositeKey = LockKeyBuilder.buildKey(LockKeyType.OFFER_TYPE, traderId, stockId, type.opposite());
                redisLockService.unlock(lockOppositeKey);
            }

        } finally {
            redisLockService.unlock(metaKey);
        }
    }

    private OfferType extractType(BaseOfferDTO dto) {
        if (dto instanceof BuyOfferDTO) return OfferType.BUY;
        if (dto instanceof SellOfferDTO) return OfferType.SELL;
        throw new BurseException(ErrorCode.INVALID_OFFER, "Unknown offer type.");
    }

    @Override
    public void cancelOffer(String offerId) {
        String lockKey = LockKeyBuilder.buildKey(LockKeyType.OFFER, offerId);
        if (redisLockService.failLock(lockKey)) {
            throw new BurseException(ErrorCode.OFFER_LOCKED, "Offer is currently locked for processing a trade.");
        }

        try {
            ActiveOffer offer = activeOfferRepository.findById(offerId)
                    .orElseThrow(() -> new BurseException(ErrorCode.OFFER_NOT_FOUND, "Offer not found"));

            archiveOffer(offer, ArchiveReason.CANCELED);
        } finally {
            redisLockService.unlock(lockKey);
        }
    }

    public List<OfferResponseDTO> getActiveOffersForStock(String stockId) {
        List<ActiveOffer> activeOffers = activeOfferRepository.findByStockId(stockId);
        return activeOffers.stream()
                .map(ActiveOffer::toResponseDTO)
                .toList();
    }

    @Override
    public List<OfferResponseDTO> getActiveOffersForTrader(String traderId) {
        List<ActiveOffer> activeOffers = activeOfferRepository.findByTraderId(traderId);
        return activeOffers.stream()
                .map(ActiveOffer::toResponseDTO)
                .toList();
    }

    public ActiveOffer fromDto(BaseOfferDTO dto) {
        Trader trader = traderService.findById(dto.getTraderId())
                .orElseThrow(() -> new BurseException(ErrorCode.TRADER_NOT_FOUND, "Trader not found"));

        Stock stock = stockService.findById(dto.getStockId())
                .orElseThrow(() -> new BurseException(ErrorCode.STOCK_NOT_FOUND, "Stock not found"));

        if (dto instanceof BuyOfferDTO) {
            return new BuyOffer(trader, stock, dto.getPrice(), dto.getAmount());
        }

        if (dto instanceof SellOfferDTO) {
            return new SellOffer(trader, stock, dto.getPrice(), dto.getAmount());
        }

        throw new BurseException(ErrorCode.INVALID_OFFER, "Unknown offer type");
    }


}

