package com.burse.bursebackend;

import com.burse.bursebackend.dtos.StockDetailDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.burse.bursebackend.dtos.offer.SellOfferDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.burse.bursebackend.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TradeExecutionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void secondTraderShouldBuyFromFirstTrader_whenPriceIsBetterThanMarket_checkStockPrice() throws Exception {
        String sellerId = "13";
        String buyerId = "18";
        String stockId = "4";
        BuyOfferDTO buy = new BuyOfferDTO();
        buy.setTraderId(sellerId);
        buy.setStockId(stockId);
        buy.setPrice(BigDecimal.valueOf(500));
        buy.setAmount(2);

        insertValidOffer(objectMapper, mockMvc, buy);

        SellOfferDTO sell = new SellOfferDTO();
        sell.setTraderId(sellerId);
        sell.setStockId(stockId);
        sell.setPrice(BigDecimal.valueOf(1));
        sell.setAmount(2);

        insertValidOffer(objectMapper, mockMvc, sell);

        BuyOfferDTO secondBuy = new BuyOfferDTO();
        secondBuy.setTraderId(buyerId);
        secondBuy.setStockId(stockId);
        secondBuy.setPrice(BigDecimal.valueOf(500));
        secondBuy.setAmount(1);

        insertValidOffer(objectMapper, mockMvc, secondBuy);

        String response = mockMvc.perform(get("/api/traders/"+ buyerId+ "/trades"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> trades = objectMapper.readValue(response, new TypeReference<>() {});
        assertEquals(1, trades.size());
        assertEquals(sellerId, trades.get(0).get("sellerId"));

        StockDetailDTO stockAfter = getStock(objectMapper, mockMvc, stockId);

        assertEquals(0, stockAfter.getCurrentPrice().compareTo(BigDecimal.valueOf(1)),
                "Stock price should not change when no trade occurs");
    }

    @Test
    void lowPriceBuyOffer_shouldNotTriggerTrade() throws Exception {
        String traderId = "20";
        String stockId = "2";
        BuyOfferDTO dto = new BuyOfferDTO();
        dto.setTraderId(traderId);
        dto.setStockId(stockId);
        dto.setPrice(BigDecimal.ONE);
        dto.setAmount(1);

        insertValidOffer(objectMapper, mockMvc, dto);

        mockMvc.perform(get("/api/traders/" + traderId + "/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/traders/" + traderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeOffers.length()").value(1));
    }


}

