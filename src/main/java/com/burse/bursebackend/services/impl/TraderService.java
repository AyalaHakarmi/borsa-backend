package com.burse.bursebackend.services.impl;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.BuyOffer;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.redis.IRedisLockService;
import com.burse.bursebackend.types.ErrorCode;
import com.burse.bursebackend.types.KeyType;
import com.burse.bursebackend.exceptions.BurseException;
import com.burse.bursebackend.redis.KeyBuilder;
import com.burse.bursebackend.repositories.TraderRepository;
import com.burse.bursebackend.services.interfaces.ITraderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TraderService implements ITraderService {

    private final TraderRepository traderRepository;
    private final IRedisLockService redisLockService;

    @Override
    public void exchangeMoneyAndStock(BuyOffer buyOffer, SellOffer sellOffer, Stock stock, BigDecimal tradeTotalPrice, int numOfStocksTraded) {
        String lockTraderMoney = KeyBuilder.buildKey(KeyType.MONEY, buyOffer.getTrader().getId());
        if (!redisLockService.tryAcquireLock(lockTraderMoney)) {
            throwTryAnotherMatch();
        }

        verifyTraderMoneyAvailable(buyOffer, tradeTotalPrice, lockTraderMoney);

        String lockTraderStock = KeyBuilder.buildKey(KeyType.STOCK, sellOffer.getTrader().getId(), stock.getId());
        if (!redisLockService.tryAcquireLock(lockTraderStock)) {
            redisLockService.unlock(lockTraderMoney);
            throwTryAnotherMatch();
        }

        verifyTraderStockAvailability(sellOffer, numOfStocksTraded, lockTraderMoney, lockTraderStock);

        updateTradersMoney(buyOffer, sellOffer, tradeTotalPrice);
        updateTradersStock(buyOffer, sellOffer, numOfStocksTraded);

        logTradeSuccess(buyOffer, sellOffer, stock, numOfStocksTraded, tradeTotalPrice);
        redisLockService.unlock(lockTraderStock, lockTraderMoney);
    }

    @Override
    public void verifyTraderStockAvailability(SellOffer sellOffer, int numOfStocksTraded, String lockTraderMoney, String lockTraderStock) {
        Stock stock = sellOffer.getStock();
        Integer traderStockAmount = sellOffer.getTrader().getHoldings().get(stock.getId());
        if (traderStockAmount == null || traderStockAmount < numOfStocksTraded) {
            log.warn("Seller {} does not have enough stock {} for trade (needed: {}, has: {}). His offer {} will be cancelled.",
                    sellOffer.getTrader().getId(),
                    stock.getId(),
                    numOfStocksTraded,
                    traderStockAmount != null ? traderStockAmount : 0,
                    sellOffer.getId());
            redisLockService.unlock(lockTraderMoney, lockTraderStock);
            throwMissingFunds(sellOffer.getId());
        }
    }

    @Override
    public void verifyTraderMoneyAvailable(BuyOffer buyOffer, BigDecimal tradeTotalPrice, String lockTraderMoney) {
        BigDecimal traderMoney = buyOffer.getTrader().getMoney();
        if (traderMoney.compareTo(tradeTotalPrice) < 0) {
            log.warn("Buyer {} does not have enough money for trade (needed: {}, has: {}). His offer {} will be cancelled.",
                    buyOffer.getTrader().getId(),
                    tradeTotalPrice,
                    buyOffer.getTrader().getMoney(),
                    buyOffer.getId());
            redisLockService.unlock(lockTraderMoney);
            throwMissingFunds(buyOffer.getId());
        }
    }


    @Override
    public void updateTradersMoney(BuyOffer buyOffer, SellOffer sellOffer, BigDecimal tradeTotalPrice) {
        Trader buyer = buyOffer.getTrader();
        Trader seller = sellOffer.getTrader();
        buyer.setMoney(buyer.getMoney().subtract(tradeTotalPrice));
        seller.setMoney(seller.getMoney().add(tradeTotalPrice));
        traderRepository.save(buyer);
        traderRepository.save(seller);
    }

    @Override
    public void updateTradersStock(BuyOffer buyOffer, SellOffer sellOffer, int numOfStocksTraded) {
        Trader buyer = buyOffer.getTrader();
        Trader seller = sellOffer.getTrader();
        Stock stock = buyOffer.getStock();

        Integer buyerStockAmount = buyer.getHoldings().getOrDefault(stock.getId(), 0);
        buyer.getHoldings().put(stock.getId(), buyerStockAmount + numOfStocksTraded);

        Integer sellerStockAmount = seller.getHoldings().getOrDefault(stock.getId(), 0);
        seller.getHoldings().put(stock.getId(), sellerStockAmount - numOfStocksTraded);

        traderRepository.save(buyer);
        traderRepository.save(seller);

    }

    @Override
    public List<String> getAllTraderNames() {
        log.debug("Fetching all trader names");
        return traderRepository.findAll()
                .stream()
                .map(Trader::getName)
                .toList();
    }

    @Override
    public Optional<Trader> findById(String traderId) {
        return traderRepository.findById(traderId);
    }

    private void throwTryAnotherMatch() {
        log.debug("Trade execution failed due to locked resources. Suggesting to try another match.");
        throw new BurseException(
                ErrorCode.TRY_ANOTHER_MATCH,
                "Try to find another potential match"

        );
    }

    private void throwMissingFunds(String offerId) {
        throw new BurseException(
                ErrorCode.MISSING_FUNDS,
                offerId
        );
    }

    private void logTradeSuccess(BuyOffer buyOffer, SellOffer sellOffer, Stock stock, int qty, BigDecimal totalPrice) {
        log.info("Trade successfully executed: buyer {} purchased {} units of stock {} from seller {} for total {}",
                buyOffer.getTrader().getId(),
                qty,
                stock.getId(),
                sellOffer.getTrader().getId(),
                totalPrice);
    }


}
