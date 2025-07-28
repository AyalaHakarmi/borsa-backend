package com.burse.bursebackend.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StrategyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCurrentStrategy_shouldReturnNonEmptyStrategy() throws Exception {
        String response = mockMvc.perform(get("/api/strategy"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response).isNotBlank();
    }

    @Test
    void getAvailableStrategies_shouldReturnAllStrategies() throws Exception {
        String response = mockMvc.perform(get("/api/strategy/available-strategies"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, String> strategies = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(strategies).isNotEmpty();
    }

    @Test
    void switchStrategy_shouldUpdateStrategySuccessfully() throws Exception {
        String strategyName = "pureRandomStrategy";

        String response = mockMvc.perform(put("/api/strategy/switch")
                        .param("strategyName", strategyName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response).contains(strategyName);
    }

    @Test
    void switchStrategy_thenVerifyCurrentStrategyWasUpdated() throws Exception {
        String available = mockMvc.perform(get("/api/strategy/available-strategies"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, String> strategies = objectMapper.readValue(available, new TypeReference<>() {});

        String current = mockMvc.perform(get("/api/strategy"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replace("\"", "");

        String otherStrategy = strategies.keySet().stream()
                .filter(name -> !name.equals(current))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No alternative strategy found"));

        String switchResponse = mockMvc.perform(put("/api/strategy/switch")
                        .param("strategyName", otherStrategy))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(switchResponse).contains(otherStrategy);

        String newCurrent = mockMvc.perform(get("/api/strategy"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replace("\"", "");

        assertThat(newCurrent).isEqualTo(otherStrategy);
    }

    @Test
    void switchToSameStrategy_shouldReturnSameStrategyMessage() throws Exception {
        String currentStrategy = mockMvc.perform(get("/api/strategy"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replace("\"", "");

        String response = mockMvc.perform(put("/api/strategy/switch")
                        .param("strategyName", currentStrategy))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response.toLowerCase()).contains("already using strategy");

    }

    @Test
    void switchToInvalidStrategy_shouldReturnBadRequest() throws Exception {
        String response = mockMvc.perform(put("/api/strategy/switch")
                        .param("strategyName", "nonExistingStrategy"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThat(response.toLowerCase()).contains("unknown strategy");
    }







}
