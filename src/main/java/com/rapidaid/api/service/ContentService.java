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
import java.util.logging.Logger;

@Service
public class ContentService {

    private static final Logger log = Logger.getLogger(ContentService.class.getName());

    private List<FirstAidResponse> firstAidList = new ArrayList<>();
    private List<TipResponse> tipList = new ArrayList<>();

    @PostConstruct
    public void loadContent() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            InputStream fa = new ClassPathResource("firstaid.json").getInputStream();
            firstAidList = mapper.readValue(fa, new TypeReference<>() {});
            log.info("Loaded " + firstAidList.size() + " first aid categories");
        } catch (Exception e) {
            log.severe("Failed to load firstaid.json: " + e.getMessage());
        }
        try {
            InputStream tips = new ClassPathResource("tips.json").getInputStream();
            tipList = mapper.readValue(tips, new TypeReference<>() {});
            log.info("Loaded " + tipList.size() + " tips");
        } catch (Exception e) {
            log.severe("Failed to load tips.json: " + e.getMessage());
        }
    }

    public List<FirstAidResponse> getFirstAid() { return firstAidList; }
    public List<TipResponse> getTips() { return tipList; }
}
