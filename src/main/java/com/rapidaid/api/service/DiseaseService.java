// Servicio de búsqueda de enfermedades.
// Al arrancar el servidor carga el archivo diseases.json con más de 100 enfermedades.
// Cuando llega una búsqueda sigue este orden:
//   1. Busca en el JSON local con comparación normalizada (sin acentos ni mayúsculas)
//   2. Si no encuentra nada, llama a Gemini AI como fallback
//   3. Los resultados de Gemini se guardan en una caché interna para evitar llamadas repetidas

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

    // Servicio de Gemini AI (usado como fallback cuando no se encuentra en el JSON local)
    private final GeminiService geminiService;

    // Lista de enfermedades cargada desde diseases.json al arrancar el servidor
    private List<Map<String, Object>> diseases;

    // Constructor: Spring inyecta GeminiService automáticamente
    public DiseaseService(GeminiService geminiService) {
        this.geminiService = geminiService;
        this.diseases = new ArrayList<>();
    }

    // Caché en memoria para resultados de Gemini — evita llamar a la API dos veces
    // con la misma consulta. ConcurrentHashMap es seguro para acceso simultáneo.
    private final Map<String, List<DiseaseResponse>> geminiCache =
        new java.util.concurrent.ConcurrentHashMap<>();

    // Se ejecuta automáticamente al arrancar el servidor.
    // Lee diseases.json y carga todas las enfermedades en memoria.
    @PostConstruct
    public void loadDiseases() {
        try {
            InputStream is = new ClassPathResource("diseases.json").getInputStream();
            ObjectMapper mapper = new ObjectMapper(); // Convierte JSON a Map automáticamente
            diseases = mapper.readValue(is, new TypeReference<>() {});
            log.info("Loaded " + diseases.size() + " diseases from JSON");
        } catch (Exception e) {
            log.severe("Failed to load diseases.json: " + e.getMessage());
        }
    }

    // Normaliza un texto eliminando acentos y convirtiendo a minúsculas.
    // Esto permite que "Diarréa" encuentre "diarrea" y viceversa.
    private String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)   // Descompone letras con acento
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "") // Elimina los diacríticos
                .toLowerCase();                                        // Convierte a minúsculas
    }

    // Método principal de búsqueda. Recibe la consulta del usuario y sigue el orden:
    // 1. Busca en el JSON local → 2. Busca en caché Gemini → 3. Llama a Gemini AI
    public List<DiseaseResponse> search(String query) {
        log.info("Searching: " + query);
        String q = normalize(query.trim()); // Normaliza la consulta antes de buscar

        // Paso 1: buscar en el JSON local
        List<DiseaseResponse> localResults = searchLocal(q);
        if (!localResults.isEmpty()) {
            log.info("Found " + localResults.size() + " local results for: " + query);
            return localResults; // Si encuentra resultados locales, los devuelve directamente
        }

        log.info("No local results for: " + query + ", trying Gemini AI");

        // Paso 2: comprobar si Gemini ya respondió antes a esta misma consulta
        if (geminiCache.containsKey(q)) {
            log.info("Returning cached Gemini result for: " + query);
            return geminiCache.get(q); // Devuelve el resultado cacheado sin llamar a Gemini
        }

        // Paso 3: llamar a Gemini AI para obtener la respuesta
        DiseaseResponse aiResult = geminiService.searchWithAI(query);
        if (aiResult != null) {
            List<DiseaseResponse> result = List.of(aiResult);
            geminiCache.put(q, result); // Guarda el resultado en caché para futuras búsquedas
            return result;
        }

        // Si Gemini tampoco encontró nada, guarda lista vacía en caché y devuelve vacío
        geminiCache.put(q, Collections.emptyList());
        return Collections.emptyList();
    }

    // Busca en la lista local de enfermedades usando comparación normalizada
    private List<DiseaseResponse> searchLocal(String query) {
        return diseases.stream()
                .filter(d -> matchesDisease(d, query)) // Filtra las que coinciden con la consulta
                .map(this::toResponse)                  // Convierte cada Map a DiseaseResponse
                .collect(Collectors.toList());
    }

    // Comprueba si una enfermedad coincide con la consulta.
    // Acepta coincidencia exacta, al inicio, en el medio o al final del nombre.
    // También comprueba los aliases (nombres alternativos) de la enfermedad.
    @SuppressWarnings("unchecked")
    private boolean matchesDisease(Map<String, Object> disease, String query) {
        // Normaliza el nombre de la enfermedad igual que la consulta
        String name = normalize((String) disease.getOrDefault("name", ""));

        // Comprueba si el nombre coincide con la consulta (como palabra completa)
        if (name.equals(query)
                || name.startsWith(query + " ")
                || name.contains(" " + query + " ")
                || name.endsWith(" " + query)) {
            return true;
        }

        // Coincidencia cuando la consulta contiene el nombre (ej. "gripe aviar" encuentra "gripe")
        if (query.startsWith(name)) {
            return true;
        }

        // Comprueba los aliases (nombres alternativos) de la enfermedad
        Object aliasesObj = disease.get("aliases");
        if (aliasesObj instanceof List<?> aliases) {
            for (Object alias : aliases) {
                String a = normalize(alias.toString()); // Normaliza cada alias
                if (a.equals(query)
                        || a.startsWith(query + " ")
                        || a.contains(" " + query + " ")
                        || a.endsWith(" " + query)
                        || query.startsWith(a)) {
                    return true;
                }
            }
        }

        return false; // No coincide con ningún criterio
    }

    // Convierte un Map (leído del JSON) a un objeto DiseaseResponse listo para enviar
    private DiseaseResponse toResponse(Map<String, Object> disease) {
        return new DiseaseResponse(
            (String) disease.getOrDefault("name", ""),        // Nombre de la enfermedad
            (String) disease.getOrDefault("description", ""), // Descripción
            (String) disease.getOrDefault("symptoms", ""),    // Síntomas
            // Si no hay tratamiento en el JSON, usa un texto genérico
            (String) disease.getOrDefault("treatment",
                "Consulte con un médico especialista para el diagnóstico y tratamiento adecuado."),
            "RapidAid DB" // Indica que el resultado viene de la base de datos local
        );
    }
}
