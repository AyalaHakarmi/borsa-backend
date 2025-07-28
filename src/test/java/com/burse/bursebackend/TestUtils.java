package com.burse.bursebackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

public class TestUtils {

    public static String toJson(ObjectMapper objectMapper, Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T fromJson(ObjectMapper objectMapper, MvcResult result, Class<T> clazz) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }
}

