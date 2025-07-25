package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trade;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.exceptions.ErrorCode;
import com.burse.bursebackend.repositories.TradeRepository;
import com.burse.bursebackend.locks.IRedisLockService;
import com.burse.bursebackend.services.ITradeService;
import com.burse.bursebackend.services.ITraderService;
import com.burse.bursebackend.locks.LockKeyBuilder;
import com.burse.bursebackend.locks.LockKeyType;
import com.burse.bursebackend.services.stocks.IStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TradeService implements ITradeService {

    private final IRedisLockService redisLockService;
    private final ITraderService traderService;
    private final TradeRepository tradeRepository;
    private final IStockService stockService;

    @Override
    @Transactional
    public void executeTrade(BuyOffer buyOffer, SellOffer sellOffer, int tradeQty) {

        BigDecimal tradePricePerUnit = getTradePrice(buyOffer, sellOffer);
        BigDecimal tradeTotalPrice = tradePricePerUnit.multiply(BigDecimal.valueOf(tradeQty));
        Stock stock = sellOffer.getStock();

        String lockTraderMoney = LockKeyBuilder.buildKey(LockKeyType.MONEY, buyOffer.getTrader().getId());

        if (redisLockService.failLock(lockTraderMoney)){
            throwTryAnotherMatch();
        }

        if (!traderService.hasEnoughMoney(buyOffer.getTrader(), tradeTotalPrice)) {
            redisLockService.unlock(lockTraderMoney);
            throwTryAnotherMatch();
        }

        String lockTraderStock = LockKeyBuilder.buildKey(LockKeyType.STOCK, sellOffer.getTrader().getId(), stock.getId());

        if (redisLockService.failLock(lockTraderStock)) {
            redisLockService.unlock(lockTraderMoney);
            throwTryAnotherMatch();
        }

        if (traderService.hasEnoughStock(sellOffer.getTrader(), stock, tradeQty)) {
            redisLockService.unlock(lockTraderMoney, lockTraderStock);
            throwTryAnotherMatch();
        }

        traderService.updateTradersMoney(buyOffer, sellOffer, tradeTotalPrice);
        traderService.updateTradersStock(buyOffer,sellOffer, tradeQty);

        redisLockService.unlock(lockTraderStock, lockTraderMoney);

        recordTrade(buyOffer, sellOffer, tradePricePerUnit,tradeQty );

        stockService.updateStockPrice(stock, tradePricePerUnit);

    }

    private void recordTrade(BuyOffer buyOffer, SellOffer sellOffer, BigDecimal tradePricePerUnit, int tradeQty) {
        Trade trade = new Trade(buyOffer, sellOffer, tradePricePerUnit, tradeQty);
        tradeRepository.save(trade);
        buyOffer.getTrader().addAsBuyTrade(trade);
        sellOffer.getTrader().addAsSellTrade(trade);
        buyOffer.getStock().addTrade(trade);
    }


    private BigDecimal getTradePrice(BuyOffer buyOffer, SellOffer sellOffer) {
        if (buyOffer.getCreatedAt().isBefore(sellOffer.getCreatedAt())) {
            return buyOffer.getPrice();
        } else {
            return sellOffer.getPrice();
        }
    }

    private void throwTryAnotherMatch() {
        throw new BurseException(
                ErrorCode.TRY_ANOTHER_MATCH,
                "Try to find another potential match"

        );
    }

}
