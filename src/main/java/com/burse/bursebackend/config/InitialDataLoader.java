package com.burse.bursebackend.config;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trader;
import com.burse.bursebackend.entities.offer.SellOffer;
import com.burse.bursebackend.repositories.StockRepository;
import com.burse.bursebackend.repositories.TraderRepository;
import com.burse.bursebackend.repositories.offer.SellOfferRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitialDataLoader {

    private static final String BURSE_TRADER_ID = "BURSE";

    private final StockRepository stockRepository;
    private final TraderRepository traderRepository;
    private final SellOfferRepository sellOfferRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadInitialData() throws Exception {
        InputStream jsonStream = getClass().getResourceAsStream("/data/BurseJson.json");
        JsonBootstrapData data = objectMapper.readValue(jsonStream, JsonBootstrapData.class);

        List<Stock> stocks = data.getShares();
        List<Trader> traders = data.getTraders();

        stockRepository.saveAll(stocks);
        traderRepository.saveAll(traders);

        Trader burse = new Trader();
        burse.setId(BURSE_TRADER_ID);
        burse.setName("Burse");
        burse.setMoney(BigDecimal.ZERO);

        burse.setHoldings(stocks.stream()
                .collect(java.util.stream.Collectors.toMap(Stock::getId, Stock::getAmount)));

        traderRepository.save(burse);

        for (Stock stock : stocks) {
            SellOffer offer = new SellOffer(
                    burse,
                    stock,
                    stock.getCurrentPrice(),
                    stock.getAmount()
            );
            sellOfferRepository.save(offer);
        }

    }
}
