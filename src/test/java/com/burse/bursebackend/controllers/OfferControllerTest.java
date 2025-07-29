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

import static com.burse.bursebackend.TestUtils.insertValidOffer;
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
    void addBuyOffer_shouldReturnOk() throws Exception {
        String stockId = "2";
        String traderId = "1";

        BuyOfferDTO dto = new BuyOfferDTO();
        dto.setTraderId(stockId);
        dto.setStockId(traderId);
        dto.setPrice(new BigDecimal("340"));
        dto.setAmount(5);

        insertValidOffer(objectMapper, mockMvc, dto);

    }

    @Test
    void addBuyOffer_withNonExistingTrader_shouldReturnBadRequest() throws Exception {
        String stockId = "2";

        BuyOfferDTO dto = new BuyOfferDTO();
        dto.setTraderId("non-existing-id");
        dto.setStockId(stockId);
        dto.setPrice(BigDecimal.valueOf(300));
        dto.setAmount(5);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertTrue(response.contains("Trader not found"));
                });
    }

    @Test
    void addBuyOffer_withNonExistingStock_shouldReturnBadRequest() throws Exception {
        String traderId = "2";

        BuyOfferDTO dto = new BuyOfferDTO();
        dto.setTraderId(traderId);
        dto.setStockId("non-existing-id");
        dto.setPrice(BigDecimal.valueOf(300));
        dto.setAmount(5);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertTrue(response.contains("Stock not found"));
                });
    }

    @Test
    void sellThenBuyOffer_sameTraderSameStock_shouldReturnError() throws Exception {
        String stockId = "3";
        String traderId = "2";

        SellOfferDTO sellDto = new SellOfferDTO();
        sellDto.setTraderId(traderId);
        sellDto.setStockId(stockId);
        sellDto.setPrice(BigDecimal.valueOf(310));
        sellDto.setAmount(1);

        insertValidOffer(objectMapper, mockMvc, sellDto);

        BuyOfferDTO buyDto = new BuyOfferDTO();
        buyDto.setTraderId(traderId);
        buyDto.setStockId(stockId);
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
