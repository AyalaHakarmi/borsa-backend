package com.burse.bursebackend.controllers;

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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void addBuyOffer_whenValid_shouldReturnOk() throws Exception {
        BuyOfferDTO dto = new BuyOfferDTO();
        dto.setTraderId("1");
        dto.setStockId("2");
        dto.setPrice(new BigDecimal("340"));
        dto.setAmount(5);

        mockMvc.perform(post("/api/offers/buy")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void addBuyOffer_withNonExistingTrader_shouldReturnBadRequest() throws Exception {
        BuyOfferDTO dto = new BuyOfferDTO();
        dto.setTraderId("non-existing-id");
        dto.setStockId("2");
        dto.setPrice(BigDecimal.valueOf(300));
        dto.setAmount(5);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBuyOffer_withNonExistingStock_shouldReturnBadRequest() throws Exception {
        BuyOfferDTO dto = new BuyOfferDTO();
        dto.setTraderId("2");
        dto.setStockId("non-existing-id");
        dto.setPrice(BigDecimal.valueOf(300));
        dto.setAmount(5);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sellThenBuyOffer_sameTraderSameStock_shouldReturnError() throws Exception {
        SellOfferDTO sellDto = new SellOfferDTO();
        sellDto.setTraderId("2");
        sellDto.setStockId("3");
        sellDto.setPrice(BigDecimal.valueOf(310));
        sellDto.setAmount(1);

        mockMvc.perform(post("/api/offers/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellDto)))
                .andExpect(status().isOk());

        BuyOfferDTO buyDto = new BuyOfferDTO();
        buyDto.setTraderId("2");
        buyDto.setStockId("3");
        buyDto.setPrice(BigDecimal.valueOf(320));
        buyDto.setAmount(1);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertTrue(response.contains("opposite type"));
                });
    }


}
