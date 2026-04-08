package com.rapidaid.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidaid.api.model.DiseaseResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class DiseaseService {

    private static final Logger log = Logger.getLogger(DiseaseService.class.getName());

    private final GeminiService geminiService;
    private List<Map<String, Object>> diseases = new ArrayList<>();

    public DiseaseService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    // Simple cache for Gemini results to avoid duplicate API calls
    private final Map<String, List<DiseaseResponse>> geminiCache = new java.util.concurrent.ConcurrentHashMap<>();

    @PostConstruct
    public void loadDiseases() {
        try {
            InputStream is = new ClassPathResource("diseases.json").getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            diseases = mapper.readValue(is, new TypeReference<>() {});
            log.info("Loaded " + diseases.size() + " diseases from JSON");
        } catch (Exception e) {
            log.severe("Failed to load diseases.json: " + e.getMessage());
        }
    }

    public List<DiseaseResponse> search(String query) {
        log.info("Searching: " + query);
        String q = query.toLowerCase().trim();

        List<DiseaseResponse> localResults = searchLocal(q);
        if (!localResults.isEmpty()) {
            log.info("Found " + localResults.size() + " local results for: " + query);
            return localResults;
        }

        log.info("No local results for: " + query + ", trying Gemini AI");

        // Check Gemini cache first to avoid duplicate API calls
        if (geminiCache.containsKey(q)) {
            log.info("Returning cached Gemini result for: " + query);
            return geminiCache.get(q);
        }

        DiseaseResponse aiResult = geminiService.searchWithAI(query);
        if (aiResult != null) {
            List<DiseaseResponse> result = List.of(aiResult);
            geminiCache.put(q, result);
            return result;
        }

        geminiCache.put(q, Collections.emptyList()); // Cache empty result too
        return Collections.emptyList();
    }

    private List<DiseaseResponse> searchLocal(String query) {
        return diseases.stream()
                .filter(d -> matchesDisease(d, query))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private boolean matchesDisease(Map<String, Object> disease, String query) {
        String name = ((String) disease.getOrDefault("name", "")).toLowerCase();

        // Match name: exact, starts with query, or query is a full word in the name
        if (name.equals(query)
                || name.startsWith(query + " ")
                || name.contains(" " + query + " ")
                || name.endsWith(" " + query)) {
            return true;
        }

        // Also match if query starts with name (e.g. query="diabetes tipo 2", name="diabetes")
        if (query.startsWith(name)) {
            return true;
        }

        // Check aliases with same word-boundary logic
        Object aliasesObj = disease.get("aliases");
        if (aliasesObj instanceof List<?> aliases) {
            for (Object alias : aliases) {
                String a = alias.toString().toLowerCase();
                if (a.equals(query)
                        || a.startsWith(query + " ")
                        || a.contains(" " + query + " ")
                        || a.endsWith(" " + query)
                        || query.startsWith(a)) {
                    return true;
                }
            }
        }

        return false;
    }

    private DiseaseResponse toResponse(Map<String, Object> disease) {
        return new DiseaseResponse(
            (String) disease.getOrDefault("name", ""),
            (String) disease.getOrDefault("description", ""),
            (String) disease.getOrDefault("symptoms", ""),
            (String) disease.getOrDefault("treatment",
                "Consulte con un médico especialista para el diagnóstico y tratamiento adecuado."),
            "RapidAid DB"
        );
    }
}
