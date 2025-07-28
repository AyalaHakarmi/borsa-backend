package com.burse.bursebackend.pricing;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.offer.*;
import com.burse.bursebackend.types.OfferType;
import com.burse.bursebackend.repositories.offer.ArchivedOfferRepository;
import com.burse.bursebackend.repositories.offer.ActiveOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Component("SentimentBasedStrategy")
public class SentimentBasedStrategy implements IStockPriceUpdateStrategy {

    private final ArchivedOfferRepository archivedRepo;
    private final ActiveOfferRepository activeRepo;
    private final Random random = new Random();


    @Override
    public BigDecimal calculateNewPrice(Stock stock) {
        BigDecimal currentPrice = stock.getCurrentPrice();
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        List<ArchivedOffer> archived = archivedRepo.findByStockIdAndTimestampAfter(stock.getId(), weekAgo);
        List<ActiveOffer> active = activeRepo.findByStockId(stock.getId());

        long totalBuy = calculateTotalVolume(archived, active, true);
        long totalSell = calculateTotalVolume(archived, active, false);

        double sentiment = calculateSentiment(totalBuy, totalSell);
        double totalChange = calculateVolatility(sentiment);

        BigDecimal multiplier = BigDecimal.valueOf(1 + totalChange);
        BigDecimal newPrice = currentPrice.multiply(multiplier);

        return newPrice.max(BigDecimal.valueOf(0.01)).setScale(2, RoundingMode.HALF_UP);
    }

    private long calculateTotalVolume(List<ArchivedOffer> archived, List<ActiveOffer> active, boolean isBuy) {
        long archivedVolume = archived.stream()
                .filter(o -> o.getOfferType() == (isBuy ? OfferType.BUY : OfferType.SELL))
                .mapToLong(ArchivedOffer::getAmount)
                .sum();

        long activeVolume = active.stream()
                .filter(o -> isBuy ? o instanceof BuyOffer : o instanceof SellOffer)
                .mapToLong(Offer::getAmount)
                .sum();

        return archivedVolume + activeVolume;
    }

    private double calculateSentiment(long totalBuy, long totalSell) {
        long total = totalBuy + totalSell;
        if (total == 0) return 0;
        return (double) (totalBuy - totalSell) / total;
    }

    private double calculateVolatility(double sentiment) {
        double baseVolatility = 0.001 + random.nextDouble() * 0.004;
        return baseVolatility + sentiment * 0.01;
    }

    @Override
    public String getDescription() {
        return "Adjusts stock price based on market sentiment, calculated from the ratio of recent buy and sell offers (both active and archived).";
    }
}
