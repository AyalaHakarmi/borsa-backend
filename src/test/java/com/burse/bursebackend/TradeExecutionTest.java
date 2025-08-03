package com.burse.bursebackend;

import com.burse.bursebackend.dtos.stock.StockDetailDTO;
import com.burse.bursebackend.dtos.TradeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.burse.bursebackend.dtos.offer.SellOfferDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
        String stockId = "15";
        BuyOfferDTO buy = new BuyOfferDTO();
        buy.setTraderId(sellerId);
        buy.setStockId(stockId);
        buy.setPrice(BigDecimal.valueOf(5000));
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

        MvcResult result = mockMvc.perform(get("/api/stocks/"+ stockId))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        StockDetailDTO stockDetail = objectMapper.readValue(responseBody, StockDetailDTO.class);
        List<TradeDTO> trades = stockDetail.getRecentTrades();
        assertNotNull(trades, "Trades list should not be null");
        assertEquals(2, trades.size(), "Expected exactly 2 trades");
        boolean hasExpectedSeller = trades.stream()
                .anyMatch(trade -> sellerId.equals(trade.getSellerId()));
        assertTrue(hasExpectedSeller, "Expected at least one trade with seller ID = " + sellerId);



        StockDetailDTO stockAfter = getStock(objectMapper, mockMvc, stockId);

        assertEquals(0, stockAfter.getCurrentPrice().compareTo(BigDecimal.valueOf(1)),
                "Stock price should not change when no trade occurs");
    }

    @Test
    void lowPriceBuyOffer_shouldNotTriggerTrade() throws Exception {
        String traderId = "20";
        String stockId = "16";
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

