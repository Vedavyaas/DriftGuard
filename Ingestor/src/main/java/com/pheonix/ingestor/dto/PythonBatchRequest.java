package com.pheonix.ingestor.dto;

import java.util.List;

public class PythonBatchRequest {
    private List<DriftEventDTO> events;

    public PythonBatchRequest() {}

    public PythonBatchRequest(List<DriftEventDTO> events) {
        this.events = events;
    }

    public List<DriftEventDTO> getEvents() { return events; }
    public void setEvents(List<DriftEventDTO> events) { this.events = events; }
}