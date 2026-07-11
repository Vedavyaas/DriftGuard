package com.pheonix.ingestor.service;

import com.pheonix.ingestor.dto.DriftEventDTO;
import com.pheonix.ingestor.dto.PythonBatchRequest;
import com.pheonix.ingestor.dto.PythonResponse;
import com.pheonix.ingestor.dto.PythonSingleRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class MLIntelligenceClient {

    private final RestTemplate restTemplate;
    private final String pythonApiUrl = "http://localhost:9003";

    public MLIntelligenceClient() {
        this.restTemplate = new RestTemplate();
    }

    public PythonResponse analyzeSingleEvent(DriftEventDTO event) {
        PythonSingleRequest request = new PythonSingleRequest(event);
        String url = pythonApiUrl + "/analyze/event";
        
        try {
            return restTemplate.postForObject(url, request, PythonResponse.class);
        } catch (Exception e) {
            System.err.println("Failed to call Python /analyze/event: " + e.getMessage());
            return null;
        }
    }

    public PythonResponse analyzeBatch(List<DriftEventDTO> events) {
        PythonBatchRequest request = new PythonBatchRequest(events);
        String url = pythonApiUrl + "/analyze/batch";
        
        try {
            return restTemplate.postForObject(url, request, PythonResponse.class);
        } catch (Exception e) {
            System.err.println("Failed to call Python /analyze/batch: " + e.getMessage());
            return null;
        }
    }
}
