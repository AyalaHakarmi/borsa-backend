package com.burse.bursebackend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.burse.bursebackend.dtos.offer.SellOfferDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.burse.bursebackend.TestUtils.toJson;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TradeExecutionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void secondTraderShouldBuyFromFirstTrader_whenPriceIsBetterThanMarket() throws Exception {
        BuyOfferDTO buy = new BuyOfferDTO();
        buy.setTraderId("13");
        buy.setStockId("4");
        buy.setPrice(BigDecimal.valueOf(500));
        buy.setAmount(2);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper,buy)))
                .andExpect(status().isOk());

        SellOfferDTO sell = new SellOfferDTO();
        sell.setTraderId("13");
        sell.setStockId("4");
        sell.setPrice(BigDecimal.valueOf(1));
        sell.setAmount(2);

        mockMvc.perform(post("/api/offers/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper,sell)))
                .andExpect(status().isOk());

        BuyOfferDTO secondBuy = new BuyOfferDTO();
        secondBuy.setTraderId("5");
        secondBuy.setStockId("4");
        secondBuy.setPrice(BigDecimal.valueOf(500));
        secondBuy.setAmount(1);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper,secondBuy)))
                .andExpect(status().isOk());

        String response = mockMvc.perform(get("/api/traders/5/trades"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> trades = objectMapper.readValue(response, new TypeReference<>() {});
        assertEquals(1, trades.size());
        assertEquals("13", trades.get(0).get("sellerId"));
    }

}

