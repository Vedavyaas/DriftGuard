package com.pheonix.ingestor.dto;

public class ManagerStatsDTO {
    private String managerName;
    private int projectCount;
    private double averageHealth;
    private int activeIncidents;

    public ManagerStatsDTO() {}

    public ManagerStatsDTO(String managerName, int projectCount, double averageHealth, int activeIncidents) {
        this.managerName = managerName;
        this.projectCount = projectCount;
        this.averageHealth = averageHealth;
        this.activeIncidents = activeIncidents;
    }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public int getProjectCount() { return projectCount; }
    public void setProjectCount(int projectCount) { this.projectCount = projectCount; }

    public double getAverageHealth() { return averageHealth; }
    public void setAverageHealth(double averageHealth) { this.averageHealth = averageHealth; }

    public int getActiveIncidents() { return activeIncidents; }
    public void setActiveIncidents(int activeIncidents) { this.activeIncidents = activeIncidents; }
}
