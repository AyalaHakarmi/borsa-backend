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

import static com.burse.bursebackend.TestUtils.toJson;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SellOfferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldArchiveSellOffer_whenTraderHasNoStock() throws Exception {
        BuyOfferDTO buyDto = new BuyOfferDTO();
        buyDto.setTraderId("3");
        buyDto.setStockId("10");
        buyDto.setPrice(BigDecimal.ONE);
        buyDto.setAmount(1);

        mockMvc.perform(post("/api/offers/buy")
                        .content(toJson(objectMapper,buyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        SellOfferDTO sellDto = new SellOfferDTO();
        sellDto.setTraderId("2");
        sellDto.setStockId("10");
        sellDto.setPrice(BigDecimal.ONE);
        sellDto.setAmount(1);

        mockMvc.perform(post("/api/offers/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper,sellDto)))
                .andExpect(status().isOk());

        TraderDTO trader = getTrader("2");
        assertTrue(trader.getActiveOffers().isEmpty(), "Expected no active offers for trader without stock");
    }


    private TraderDTO getTrader(String id) throws Exception {
        String res = mockMvc.perform(get("/api/traders/" + id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(res, TraderDTO.class);
    }
}

