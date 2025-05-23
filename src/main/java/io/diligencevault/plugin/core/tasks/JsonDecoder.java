package io.diligencevault.plugin.core.tasks;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDecoder {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Object parseJsonFlexible(String input) {
        try {
            // Read the input as a generic Object (can be Map or List)
            Object result = objectMapper.readValue(input, Object.class);

            if (result instanceof Map) {
                return (Map<String, Object>) result;
            } else if (result instanceof List) {
                return (List<Object>) result;
            } else {
                return result;  // Fallback for primitive JSON (e.g., "true", "123", etc.)
            }

        } catch (JsonProcessingException e) {
            // If it's not valid JSON, return the raw string
            return input;
        }
    }
}
