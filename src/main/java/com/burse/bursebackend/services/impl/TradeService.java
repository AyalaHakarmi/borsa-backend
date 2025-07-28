package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trade;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.ActiveOffer;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.locks.RedisLockService;
import com.burse.bursebackend.types.ArchiveReason;
import com.burse.bursebackend.types.LockKeyType;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.types.ErrorCode;
import com.burse.bursebackend.locks.LockKeyBuilder;
import com.burse.bursebackend.repositories.TradeRepository;
import com.burse.bursebackend.services.IOfferService;
import com.burse.bursebackend.services.ITradeService;
import com.burse.bursebackend.services.ITraderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService implements ITradeService {

    private final ITraderService traderService;
    private final TradeRepository tradeRepository;
    private final RedisLockService redisLockService;
    private final IOfferService offerService;
    private final TradeExecutionService tradeExecutionService;


    @Override
    public void searchPotentialTrade(ActiveOffer newOffer) {
        log.info("Trying to find potential trade for offer {} of trader {} on stock {}", newOffer.getId(),
            newOffer.getTrader().getId(), newOffer.getStock().getId());
        while (true) {
            Pair<BuyOffer, SellOffer> pair = offerService.findMatchingOffer(newOffer);
            BuyOffer buyOffer = pair.getLeft();
            SellOffer sellOffer = pair.getRight();
            if (buyOffer == null || sellOffer == null) {
                log.info("No matching offer found for offer {}", newOffer.getId());
                return;
            }

            String lockSellOffer = LockKeyBuilder.buildKey(LockKeyType.OFFER, sellOffer.getId());
            String lockBuyOffer = LockKeyBuilder.buildKey(LockKeyType.OFFER, buyOffer.getId());
            if (!lockOffers(buyOffer, sellOffer, lockBuyOffer, lockSellOffer)) continue;

            int numOfStocksTraded ;
            try {
                numOfStocksTraded = tradeExecutionService.executeTrade(buyOffer, sellOffer);
            } catch (BurseException ex) {
                if (ex.getErrorCode() == ErrorCode.MISSING_FUNDS){
                    offerService.cancelOffer(ex.getMessage(), ArchiveReason.NO_FUNDS_AUTO_CANCEL);
                    redisLockService.unlock(lockBuyOffer, lockSellOffer);
                    if(Objects.equals(ex.getMessage(), newOffer.getId())) break;
                    continue;
                }
                else if (ex.getErrorCode() == ErrorCode.TRY_ANOTHER_MATCH) {
                    redisLockService.unlock(lockBuyOffer, lockSellOffer);
                    continue;
                }
                throw ex;
            }
            handleRemainingOffers(buyOffer, sellOffer, numOfStocksTraded, lockBuyOffer, lockSellOffer);
            break;
        }
    }

    private void handleRemainingOffers(BuyOffer buyOffer, SellOffer sellOffer, int numOfStocksTraded, String lockBuyOffer, String lockSellOffer) {
        int remainingBuyOfferAmount = buyOffer.getAmount() - numOfStocksTraded;
        int remainingSellOfferAmount = sellOffer.getAmount() - numOfStocksTraded;
        offerService.reduceOfferAmount(buyOffer, numOfStocksTraded);
        offerService.reduceOfferAmount(sellOffer, numOfStocksTraded);

        if (remainingBuyOfferAmount == 0 && remainingSellOfferAmount == 0) {
            offerService.archiveOffer(buyOffer, ArchiveReason.COMPLETED);
            offerService.archiveOffer(sellOffer, ArchiveReason.COMPLETED);
            redisLockService.unlock(lockBuyOffer, lockSellOffer);
        } else if (remainingBuyOfferAmount > 0) {
            offerService.archiveOffer(sellOffer, ArchiveReason.COMPLETED);
            redisLockService.unlock(lockSellOffer, lockBuyOffer);
            searchPotentialTrade(buyOffer);
        } else if (remainingSellOfferAmount > 0) {
            offerService.archiveOffer(buyOffer, ArchiveReason.COMPLETED);
            redisLockService.unlock(lockBuyOffer, lockSellOffer);
            searchPotentialTrade(sellOffer);
        }

    }

    private boolean lockOffers(BuyOffer buyOffer, SellOffer sellOffer, String lockBuyOffer, String lockSellOffer) {
        if (redisLockService.failLock(lockBuyOffer)){
            log.debug("Failed to lock buy offer {}. trying to find another match", buyOffer.getId());
            return false;
        }
        if (redisLockService.failLock(lockSellOffer)) {
            log.debug("Failed to lock sell offer {}. trying to find another match", sellOffer.getId());
            redisLockService.unlock(lockBuyOffer);
            return false;
        }
        return true;
    }

    public List<Trade> get10RecentTradesForStock(Stock stock) {
        log.debug("Fetching 10 recent trades for stockId: {}", stock.getId());
        return tradeRepository.findTop10ByStockOrderByTimestampDesc(stock);
    }

    public List<Trade> get8RecentTradesForTrader(String traderId) {
        Optional<Trader> traderOpt = traderService.findById(traderId);
        if (traderOpt.isEmpty()) {
            log.warn("Trader not found with id: {}. Cannot fetch recent trades.", traderId);
            throw new BurseException(ErrorCode.TRADER_NOT_FOUND, "Trader not found with id: " + traderId);
        }

        log.debug("Fetching 8 recent trades for traderId: {}", traderId);
        Trader trader = traderOpt.get();
        return tradeRepository.findTop8ByBuyerOrSellerOrderByTimestampDesc(trader, trader);
    }

}
