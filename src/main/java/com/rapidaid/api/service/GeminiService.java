// Servicio de consulta a Gemini AI (Google).
// Se usa como fallback cuando una enfermedad no está en el JSON local.
// Envía una consulta a la API de Gemini y parsea la respuesta JSON
// para construir un objeto DiseaseResponse.
// Requiere la variable de entorno GEMINI_API_KEY configurada en Render.

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

    // URL base de la API de Gemini con el modelo a usar
    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    // Clave de la API de Gemini, leída desde la variable de entorno de Render
    @Value("${GEMINI_API_KEY:#{null}}")
    private String apiKey;

    // Cliente HTTP para llamar a la API de Gemini
    private RestTemplate restTemplate;

    // Convierte JSON a objetos Java y viceversa
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Se ejecuta al arrancar el servidor. Configura el cliente HTTP con timeouts.
    @jakarta.annotation.PostConstruct
    public void init() {
        // Configura timeouts para evitar que el servidor quede bloqueado esperando a Gemini
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 segundos para conectar
        factory.setReadTimeout(30000);    // 30 segundos para recibir la respuesta
        this.restTemplate = new RestTemplate(factory);
    }

    // Llama a la API de Gemini con la consulta del usuario y devuelve un DiseaseResponse.
    // Devuelve null si la API no está configurada, si Gemini no reconoce la enfermedad
    // o si ocurre cualquier error en la llamada.
    public DiseaseResponse searchWithAI(String query) {
        // Si no hay API key configurada, no se puede usar Gemini
        if (apiKey == null || apiKey.isBlank()) {
            log.warning("GEMINI_API_KEY no configurada — búsqueda IA desactivada");
            return null;
        }

        log.info("Consultando Gemini AI para: " + query);

        // Construye el prompt que se envía a Gemini — le pide que responda SOLO con JSON
        String prompt = "Enfermedad: " + query +
            ". Responde SOLO con JSON: {\"name\":\"nombre\",\"description\":\"descripcion breve\"," +
            "\"symptoms\":\"sintomas\",\"treatment\":\"tratamiento\"}";

        try {
            // Construye el cuerpo de la petición en el formato que espera la API de Gemini
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt) // El texto del prompt
                    ))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.2,              // Baja temperatura = respuestas más precisas
                    "maxOutputTokens", 1024,          // Longitud máxima de la respuesta
                    "responseMimeType", "application/json" // Pide respuesta en formato JSON
                )
            );

            // Configura las cabeceras HTTP de la petición
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // El cuerpo es JSON

            // Envía la petición POST a la API de Gemini y recibe la respuesta como texto
            String fullResponseBody = restTemplate.postForObject(
                GEMINI_URL + apiKey,                       // URL con la clave de API
                new HttpEntity<>(requestBody, headers),    // Cuerpo + cabeceras
                String.class                               // Recibe la respuesta como String
            );

            // Parsea la respuesta si no está vacía
            if (fullResponseBody != null) {
                return parseGeminiResponse(fullResponseBody, query);
            } else {
                log.warning("Gemini devolvió respuesta vacía");
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warning("Gemini HTTP error: " + e.getStatusCode()); // Error HTTP (ej. 400, 403)
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.warning("Gemini timeout/conexión: " + e.getMessage()); // Timeout o sin red
        } catch (Exception e) {
            log.warning("Gemini error inesperado: " + e.getClass().getSimpleName()); // Otro error
        }

        return null; // Si algo falla, devuelve null para que DiseaseService devuelva lista vacía
    }

    // Parsea la respuesta cruda de Gemini y extrae el JSON con los datos de la enfermedad.
    // La respuesta de Gemini viene envuelta en una estructura de candidatos — hay que extraer
    // el texto del primer candidato y luego parsear el JSON dentro de ese texto.
    private DiseaseResponse parseGeminiResponse(String responseBody, String originalQuery) {
        try {
            // Navega por la estructura de la respuesta de Gemini para llegar al texto
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root
                .path("candidates").get(0)    // Primer candidato (Gemini puede devolver varios)
                .path("content")
                .path("parts").get(0)         // Primera parte del contenido
                .path("text").asText();        // Texto de la respuesta

            // Extrae el JSON del texto (puede venir con texto adicional alrededor)
            int start = text.indexOf("{");          // Posición del primer {
            int end   = text.lastIndexOf("}") + 1; // Posición después del último }

            // Si no hay llaves, la respuesta no contiene JSON válido
            if (start < 0 || end <= start) {
                log.warning("No se encontró JSON en la respuesta de Gemini");
                return null;
            }

            // Extrae solo el fragmento JSON y lo parsea
            String jsonStr = text.substring(start, end);
            JsonNode data  = objectMapper.readTree(jsonStr);

            // Si Gemini indica que no reconoce la enfermedad, devuelve null
            if (data.has("error")) return null;

            // Construye el objeto DiseaseResponse con los datos extraídos de Gemini
            return new DiseaseResponse(
                data.path("name").asText(originalQuery),    // Nombre (usa la consulta si está vacío)
                data.path("description").asText(""),        // Descripción
                data.path("symptoms").asText(""),           // Síntomas
                data.path("treatment").asText("Consulte con un médico especialista."), // Tratamiento
                "Gemini AI"                                 // Indica que el resultado es de IA
            );

        } catch (Exception e) {
            log.warning("Error procesando respuesta de Gemini: " + e.getMessage());
            return null;
        }
    }
}
