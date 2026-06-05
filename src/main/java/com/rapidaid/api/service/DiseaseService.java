package com.rapidaid.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidaid.api.model.DiseaseResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.text.Normalizer;
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

    // Elimina acentos y normaliza a minúsculas para comparación flexible
    private String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    public List<DiseaseResponse> search(String query) {
        log.info("Searching: " + query);
        String q = normalize(query.trim());

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

        geminiCache.put(q, Collections.emptyList());
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
        // Normalizar el nombre almacenado igual que la query (sin acentos, minúsculas)
        String name = normalize((String) disease.getOrDefault("name", ""));

        if (name.equals(query)
                || name.startsWith(query + " ")
                || name.contains(" " + query + " ")
                || name.endsWith(" " + query)) {
            return true;
        }

        if (query.startsWith(name)) {
            return true;
        }

        // Aliases también normalizados
        Object aliasesObj = disease.get("aliases");
        if (aliasesObj instanceof List<?> aliases) {
            for (Object alias : aliases) {
                String a = normalize(alias.toString());
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
