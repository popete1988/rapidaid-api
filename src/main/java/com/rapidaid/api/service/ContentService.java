// Servicio que carga y sirve el contenido estático de la app (primeros auxilios y consejos).
// Al arrancar el servidor (@PostConstruct) lee los archivos JSON del classpath:
//   - firstaid.json: categorías y subtipos de primeros auxilios
//   - tips.json: consejos y recomendaciones de salud
//   - version.json: número de versión del contenido (se incrementa al guardar desde el dashboard)
// El contenido se guarda en memoria y se sirve directamente sin acceder a disco en cada petición.

package com.rapidaid.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidaid.api.model.FirstAidResponse;
import com.rapidaid.api.model.TipResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// @Service indica que esta clase es un componente de servicio gestionado por Spring
@Service
public class ContentService {

    private static final Logger log = Logger.getLogger(ContentService.class.getName());

    // Listas en memoria con el contenido cargado desde los JSON
    private List<FirstAidResponse> firstAidList = new ArrayList<>(); // Categorías de primeros auxilios
    private List<TipResponse> tipList = new ArrayList<>();           // Consejos de salud
    private int contentVersion = 1;                                  // Versión del contenido (1 por defecto)

    // Se ejecuta automáticamente al arrancar el servidor.
    // Lee los tres archivos JSON y carga su contenido en memoria.
    @PostConstruct
    public void loadContent() {
        ObjectMapper mapper = new ObjectMapper(); // Convierte JSON a objetos Java automáticamente

        // Carga las categorías de primeros auxilios desde firstaid.json
        try {
            InputStream fa = new ClassPathResource("firstaid.json").getInputStream();
            firstAidList = mapper.readValue(fa, new TypeReference<>() {});
            log.info("Loaded " + firstAidList.size() + " first aid categories");
        } catch (Exception e) {
            log.severe("Failed to load firstaid.json: " + e.getMessage());
        }

        // Carga los consejos desde tips.json
        try {
            InputStream tips = new ClassPathResource("tips.json").getInputStream();
            tipList = mapper.readValue(tips, new TypeReference<>() {});
            log.info("Loaded " + tipList.size() + " tips");
        } catch (Exception e) {
            log.severe("Failed to load tips.json: " + e.getMessage());
        }

        // Carga el número de versión desde version.json
        try {
            InputStream v = new ClassPathResource("version.json").getInputStream();
            Map<String, Object> vData = mapper.readValue(v, new TypeReference<>() {});
            contentVersion = (Integer) vData.getOrDefault("version", 1); // 1 si no existe el campo
            log.info("Content version: " + contentVersion);
        } catch (Exception e) {
            log.warning("Failed to load version.json, defaulting to version 1");
        }
    }

    // Devuelve la lista de categorías de primeros auxilios cargada en memoria
    public List<FirstAidResponse> getFirstAid() { return firstAidList; }

    // Devuelve la lista de consejos cargada en memoria
    public List<TipResponse> getTips() { return tipList; }

    // Devuelve el número de versión del contenido
    public int getVersion() { return contentVersion; }
}
