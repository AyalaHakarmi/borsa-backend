package com.burse.bursebackend;

import com.burse.bursebackend.dtos.stock.StockDetailDTO;
import com.burse.bursebackend.dtos.TraderDTO;
import com.burse.bursebackend.dtos.offer.BaseOfferDTO;
import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.burse.bursebackend.dtos.offer.SellOfferDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestUtils {

    public static String toJson(ObjectMapper objectMapper, Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    public static void insertValidOffer(ObjectMapper objectMapper, MockMvc mockMvc, BaseOfferDTO offer) throws Exception {
        if (offer instanceof BuyOfferDTO) {
            mockMvc.perform(post("/api/offers/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(objectMapper,offer)))
                    .andExpect(status().isOk());
        } else if (offer instanceof SellOfferDTO) {
            mockMvc.perform(post("/api/offers/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(objectMapper,offer)))
                    .andExpect(status().isOk());
        } else {
            throw new IllegalArgumentException("Unsupported offer type: " + offer.getClass().getSimpleName());
        }
    }

    public static StockDetailDTO getStock(ObjectMapper objectMapper, MockMvc mockMvc, String stockId) throws Exception {
        String res = mockMvc.perform(get("/api/stocks/" + stockId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(res, StockDetailDTO.class);
    }

    public static TraderDTO getTrader(ObjectMapper objectMapper, MockMvc mockMvc, String id) throws Exception {
        String res = mockMvc.perform(get("/api/traders/" + id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(res, TraderDTO.class);
    }


}

