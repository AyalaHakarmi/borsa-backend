package com.burse.bursebackend;

import com.burse.bursebackend.dtos.TraderDTO;
import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.burse.bursebackend.dtos.offer.SellOfferDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.burse.bursebackend.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MissingFundsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldArchiveSellOffer_whenTraderHasNoStock() throws Exception {
        String stockId = "10";
        String buyerId = "3";
        String sellerId = "2";

        BuyOfferDTO buyDto = new BuyOfferDTO();
        buyDto.setTraderId(buyerId);
        buyDto.setStockId(stockId);
        buyDto.setPrice(BigDecimal.ONE);
        buyDto.setAmount(1);

        insertValidOffer(objectMapper, mockMvc, buyDto );

        SellOfferDTO sellDto = new SellOfferDTO();
        sellDto.setTraderId(sellerId);
        sellDto.setStockId(stockId);
        sellDto.setPrice(BigDecimal.ONE);
        sellDto.setAmount(1);

        insertValidOffer(objectMapper, mockMvc, sellDto);

        TraderDTO trader = getTrader(objectMapper, mockMvc, sellerId);
        assertTrue(trader.getActiveOffers().isEmpty(), "Expected no active offers for trader without stock");
    }

    @Test
    void buyOfferWithoutEnoughMoney_shouldBeArchivedImmediately_thenUnlockChecking() throws Exception {
        String stockId = "3";
        String traderId = "4";

        BuyOfferDTO bigBuy = new BuyOfferDTO();
        bigBuy.setTraderId(traderId);
        bigBuy.setStockId(stockId);
        bigBuy.setPrice(BigDecimal.valueOf(10000));
        bigBuy.setAmount(1000);

        insertValidOffer(objectMapper, mockMvc, bigBuy);

        TraderDTO trader = getTrader(objectMapper, mockMvc,traderId);
        assertTrue(trader.getActiveOffers().isEmpty(), "Expected no active offers for trader who lacks sufficient funds");

        BuyOfferDTO smallBuy = new BuyOfferDTO();
        smallBuy.setTraderId(traderId);
        smallBuy.setStockId(stockId);
        smallBuy.setPrice(BigDecimal.valueOf(10000));
        smallBuy.setAmount(1);

        insertValidOffer(objectMapper, mockMvc, smallBuy);

        mockMvc.perform(get("/api/traders/"+ traderId +"/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }


}

