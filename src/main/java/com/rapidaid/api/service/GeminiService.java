package com.rapidaid.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidaid.api.model.DiseaseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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

    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @jakarta.annotation.PostConstruct
    public void init() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(factory);
    }

    public DiseaseResponse searchWithAI(String query) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warning("GEMINI_API_KEY no configurada — búsqueda IA desactivada");
            return null;
        }
        log.info("Consultando Gemini AI para: " + query);

        String prompt = "Enfermedad: " + query + ". Responde SOLO con JSON: {\"name\":\"nombre\",\"description\":\"descripcion breve\",\"symptoms\":\"sintomas\",\"treatment\":\"tratamiento\"}";

        try {
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.2,
                    "maxOutputTokens", 1024,
                    "responseMimeType", "application/json"
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String fullResponseBody = restTemplate.postForObject(
                GEMINI_URL + apiKey,
                new HttpEntity<>(requestBody, headers),
                String.class
            );

            if (fullResponseBody != null) {
                return parseGeminiResponse(fullResponseBody, query);
            } else {
                log.warning("Gemini devolvió respuesta vacía");
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warning("Gemini HTTP error: " + e.getStatusCode());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.warning("Gemini timeout/conexión: " + e.getMessage());
        } catch (Exception e) {
            log.warning("Gemini error inesperado: " + e.getClass().getSimpleName());
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

            int start = text.indexOf("{");
            int end   = text.lastIndexOf("}") + 1;

            if (start < 0 || end <= start) {
                log.warning("No se encontró JSON en la respuesta de Gemini");
                return null;
            }

            String jsonStr = text.substring(start, end);
            JsonNode data  = objectMapper.readTree(jsonStr);

            if (data.has("error")) return null;

            return new DiseaseResponse(
                data.path("name").asText(originalQuery),
                data.path("description").asText(""),
                data.path("symptoms").asText(""),
                data.path("treatment").asText("Consulte con un médico especialista."),
                "Gemini AI"
            );

        } catch (Exception e) {
            log.warning("Error procesando respuesta de Gemini: " + e.getMessage());
            return null;
        }
    }
}
