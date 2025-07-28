package com.burse.bursebackend;

import com.burse.bursebackend.dtos.TraderDTO;
import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static com.burse.bursebackend.TestUtils.*;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BuyOfferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void lowPriceBuyOffer_shouldNotTriggerTrade() throws Exception {
        BuyOfferDTO dto = new BuyOfferDTO();
        dto.setTraderId("1");
        dto.setStockId("2");
        dto.setPrice(BigDecimal.ONE);
        dto.setAmount(1);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper,dto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/traders/1/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/traders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeOffers.length()").value(1));
    }

    @Test
    void buyOfferWithoutEnoughMoney_shouldBeArchivedImmediately_thenUnlockChecking() throws Exception {
        BuyOfferDTO bigBuy = new BuyOfferDTO();
        bigBuy.setTraderId("4");
        bigBuy.setStockId("3");
        bigBuy.setPrice(BigDecimal.valueOf(10000));
        bigBuy.setAmount(1000);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper,bigBuy)))
                .andExpect(status().isOk());

        TraderDTO trader = getTrader("4");
        assertTrue(trader.getActiveOffers().isEmpty(), "Expected no active offers for trader who lacks sufficient funds");

        BuyOfferDTO smallBuy = new BuyOfferDTO();
        smallBuy.setTraderId("4");
        smallBuy.setStockId("3");
        smallBuy.setPrice(BigDecimal.valueOf(10000));
        smallBuy.setAmount(1);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper,smallBuy)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/traders/4/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    private TraderDTO getTrader(String id) throws Exception {
        String res = mockMvc.perform(get("/api/traders/" + id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(res, TraderDTO.class);
    }
}

