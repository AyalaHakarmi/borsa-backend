package com.burse.bursebackend;
import com.burse.bursebackend.dtos.offer.BuyOfferDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.burse.bursebackend.TestUtils.insertValidOffer;
import static org.aspectj.bridge.MessageUtil.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ConcurrentTradeExecutionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCompleteAllTradesWithoutRaceConditions() throws Exception {
        int threadCount = 8;
        int amount = 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 1; i <= threadCount; i++) {
            int traderNum = i;
            executor.submit(() -> {
                try {
                    BuyOfferDTO dto = new BuyOfferDTO();
                    dto.setTraderId(String.valueOf(traderNum));
                    dto.setStockId(String.valueOf(traderNum));
                    dto.setPrice(BigDecimal.valueOf(1500));
                    dto.setAmount(amount);

                    insertValidOffer(objectMapper, mockMvc, dto);

                } catch (Exception e) {
                    System.err.println("Thread " + traderNum + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        for (int i = 1; i <= threadCount; i++) {
            String traderId = String.valueOf(i);

            mockMvc.perform(get("/api/traders/" + traderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.activeOffers.length()").value(0));

            mockMvc.perform(get("/api/traders/" + traderId + "/trades"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Test
    void threeTradersBuyingSameStockConcurrently_shouldAllSucceed() throws Exception {
        String stockId = "5";
        int amount = 1;
        int threadCount =3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 9; i < 9+threadCount; i++) {
            int traderId = i;
            executor.submit(() -> {
                try {
                    BuyOfferDTO dto = new BuyOfferDTO();
                    dto.setTraderId("" + (traderId));
                    dto.setStockId(stockId);
                    dto.setPrice(BigDecimal.valueOf(5000));
                    dto.setAmount(amount);

                    insertValidOffer(objectMapper, mockMvc, dto);
                } catch (Exception e) {
                    fail("Thread failed with exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        for (int i = 9; i < 9+threadCount; i++) {
            String response = mockMvc.perform(get("/api/traders/" + i + "/trades"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<?> trades = objectMapper.readValue(response, List.class);
            assertEquals(1, trades.size(), "Trader " + i + " should have 1 trade");
        }
    }

}
