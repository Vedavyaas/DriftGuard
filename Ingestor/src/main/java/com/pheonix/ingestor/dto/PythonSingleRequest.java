package com.pheonix.ingestor.dto;

public class PythonSingleRequest {
    private DriftEventDTO event;

    public PythonSingleRequest() {}

    public PythonSingleRequest(DriftEventDTO event) {
        this.event = event;
    }

    public DriftEventDTO getEvent() { return event; }
    public void setEvent(DriftEventDTO event) { this.event = event; }
}
