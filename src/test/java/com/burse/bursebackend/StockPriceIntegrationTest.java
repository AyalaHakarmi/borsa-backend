package com.burse.bursebackend;

import com.burse.bursebackend.dtos.StockDetailDTO;
import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static com.burse.bursebackend.TestUtils.toJson;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StockPriceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void stockPrice_shouldRemainStableWithoutTrade() throws Exception {
        String traderId = "11";
        String stockId = "4";

        StockDetailDTO stockBefore = getStock(stockId);

        BuyOfferDTO buy = new BuyOfferDTO();
        buy.setTraderId(traderId);
        buy.setStockId(stockId);
        buy.setPrice(stockBefore.getCurrentPrice());
        buy.setAmount(1);

        mockMvc.perform(post("/api/offers/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper,buy)))
                .andExpect(status().isOk());

        StockDetailDTO stockAfter = getStock(stockId);

        assertEquals(0, stockAfter.getCurrentPrice().compareTo(stockBefore.getCurrentPrice()),
                "Stock price should not change when no trade occurs");
    }

    private StockDetailDTO getStock(String stockId) throws Exception {
        String res = mockMvc.perform(get("/api/stocks/" + stockId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(res, StockDetailDTO.class);
    }
}

