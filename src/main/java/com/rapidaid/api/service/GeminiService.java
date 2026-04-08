package com.rapidaid.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidaid.api.model.DiseaseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class GeminiService {

    private static final Logger log = Logger.getLogger(GeminiService.class.getName());

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    @Value("${GEMINI_API_KEY:#{null}}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GeminiService() {
        // Set explicit timeouts so Gemini calls don't hang indefinitely
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 seconds
        factory.setReadTimeout(30000);    // 30 seconds
        this.restTemplate = new RestTemplate(factory);
    }
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Calls Gemini API to get structured disease information in Spanish.
     * Returns null if the call fails or the disease is not recognized.
     */
    public DiseaseResponse searchWithAI(String query) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warning("GEMINI_API_KEY not configured — skipping AI search");
            return null;
        }
        log.info("GeminiService v16 | Calling Gemini AI for: " + query + " | key starts with: " + 
            (apiKey.length() > 8 ? apiKey.substring(0, 8) + "..." : "TOO_SHORT"));

        String prompt = """
            Enfermedad: %s
            Responde SOLO con este JSON, sin markdown, sin explicaciones:
            {"name":"nombre","description":"que es en 1 frase","symptoms":"sintoma1, sintoma2, sintoma3","treatment":"tratamiento en 1 frase"}
            Si no es enfermedad real: {"error":"no_disease"}
            """.formatted(query);

        try {
            log.info("Sending request to Gemini API...");
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.2,
                    "maxOutputTokens", 300
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_URL + apiKey,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
            );

            log.info("Gemini response status: " + response.getStatusCode());
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Gemini response received, parsing...");
                return parseGeminiResponse(response.getBody(), query);
            } else {
                log.warning("Gemini non-OK response: " + response.getStatusCode() + " | " + response.getBody());
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warning("Gemini HTTP error: " + e.getStatusCode() + " | " + e.getResponseBodyAsString());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.warning("Gemini timeout/connection error: " + e.getMessage());
        } catch (Exception e) {
            log.warning("Gemini unexpected error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        return null;
    }

    private DiseaseResponse parseGeminiResponse(String responseBody, String originalQuery) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text").asText();
            
            log.info("Gemini raw text: " + text.substring(0, Math.min(text.length(), 300)));
            // Debug: print char codes around the { character
            int bracePos = text.indexOf("{");
            int bracePos2 = text.indexOf("\u007b"); // unicode {
            log.info("DEBUG indexOf({)=" + bracePos + " | text.length=" + text.length() + 
                     " | first10chars=" + text.chars().limit(20)
                         .mapToObj(c -> String.valueOf((int)c))
                         .collect(java.util.stream.Collectors.joining(",")));

            // Remove ALL markdown formatting and extract pure JSON
            // Strategy: find first { and last } in the entire text
            int start = text.indexOf("{");
            int end   = text.lastIndexOf("}") + 1;
            log.info("DEBUG start=" + start + " | end=" + end + " | text.length=" + text.length() + 
                     " | last20chars=" + text.substring(Math.max(0, text.length()-20)));
            if (start < 0 || end <= start) {
                log.warning("No JSON object found in Gemini response: " + text.substring(0, Math.min(text.length(), 100)));
                return null;
            }

            String jsonStr = text.substring(start, end);
            log.info("Parsed JSON: " + jsonStr.substring(0, Math.min(jsonStr.length(), 200)));
            JsonNode data  = objectMapper.readTree(jsonStr);

            // If Gemini says it's not a disease
            if (data.has("error")) return null;

            return new DiseaseResponse(
                data.path("name").asText(originalQuery),
                data.path("description").asText(""),
                data.path("symptoms").asText(""),
                data.path("treatment").asText("Consulte con un médico especialista."),
                "Gemini AI"
            );

        } catch (Exception e) {
            log.warning("Error parsing Gemini response: " + e.getMessage());
            return null;
        }
    }
}
