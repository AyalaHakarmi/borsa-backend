package com.burse.bursebackend.services.impl.stocks;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.offer.*;
import com.burse.bursebackend.repositories.offer.ArchivedOfferRepository;
import com.burse.bursebackend.repositories.offer.ActiveOfferRepository;
import com.burse.bursebackend.services.stocks.IStockPriceUpdateStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component("comprehensiveStrategy")
public class ComprehensiveMarketStrategy implements IStockPriceUpdateStrategy {

    private final ArchivedOfferRepository archivedRepo;
    private final ActiveOfferRepository activeRepo;
    private final Random random = new Random();

    public ComprehensiveMarketStrategy(ArchivedOfferRepository archivedRepo,
                                       ActiveOfferRepository activeRepo) {
        this.archivedRepo = archivedRepo;
        this.activeRepo = activeRepo;
    }

    @Override
    public BigDecimal calculateNewPrice(Stock stock) {
        BigDecimal currentPrice = stock.getCurrentPrice();
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        // -----------------------------
        // ניתוח הצעות בארכיון
        // -----------------------------
        List<ArchivedOffer> archived = archivedRepo.findByStockIdAndTimestampAfter(stock.getId(), weekAgo);

        long buyArchived = archived.stream()
                .filter(o -> o.getOfferType() == OfferType.BUY)
                .mapToLong(ArchivedOffer::getAmount)
                .sum();

        long sellArchived = archived.stream()
                .filter(o -> o.getOfferType() == OfferType.SELL)
                .mapToLong(ArchivedOffer::getAmount)
                .sum();

        // -----------------------------
        // ניתוח הצעות פעילות
        // -----------------------------
        List<ActiveOffer> active = activeRepo.findByStockId(stock.getId());

        long buyActive = active.stream()
                .filter(o -> o instanceof BuyOffer)
                .mapToLong(Offer::getAmount)
                .sum();

        long sellActive = active.stream()
                .filter(o -> o instanceof SellOffer)
                .mapToLong(Offer::getAmount)
                .sum();

        long totalBuy = buyArchived + buyActive;
        long totalSell = sellArchived + sellActive;

        // -----------------------------
        // חישוב מגמת שוק
        // -----------------------------
        double sentiment = 0;
        if (totalBuy + totalSell > 0) {
            sentiment = (double) (totalBuy - totalSell) / (totalBuy + totalSell);
        }

        // -----------------------------
        // תנודתיות בסיסית רנדומלית
        // -----------------------------
        double baseVolatility = 0.005 + random.nextDouble() * 0.015;
        double totalChange = baseVolatility + sentiment * 0.03;

        BigDecimal multiplier = BigDecimal.valueOf(1 + totalChange);
        BigDecimal newPrice = currentPrice.multiply(multiplier);

        return newPrice.max(BigDecimal.valueOf(0.01)).setScale(2, RoundingMode.HALF_UP);
    }
}
