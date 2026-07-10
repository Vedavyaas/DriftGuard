package com.pheonix.ingestor.config;

import com.pheonix.ingestor.assets.ProjectCreationCredentials;
import com.pheonix.ingestor.assets.HashUtil;
import com.pheonix.ingestor.assets.Status;
import com.pheonix.ingestor.repository.ProjectManagerEntity;
import com.pheonix.ingestor.repository.ProjectManagerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final ProjectManagerRepository projectManagerRepository;

    public DataSeeder(ProjectManagerRepository projectManagerRepository) {
        this.projectManagerRepository = projectManagerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (projectManagerRepository.count() == 0) {
            String sampleJson = """
            {
              "version": "1.0",
              "project_name": "Phoenix-Core",
              "description": "Sample configuration for DriftGuard ingestion",
              "cloud": {
                "provider": "AWS",
                "region": "us-east-1",
                "resources_to_track": [
                  "EC2",
                  "S3",
                  "RDS"
                ]
              },
              "git": {
                "provider": "GitHub",
                "repository": "https://github.com/company/phoenix-core",
                "default_branch": "main"
              },
              "drift_tolerances": {
                "severity": "HIGH",
                "alerting": {
                  "email": "manager@company.com",
                  "slack_channel": "#alerts-phoenix"
                }
              }
            }
            """;

            ProjectManagerEntity projectManagerEntity = new ProjectManagerEntity("CGManager", "Sample Project", sampleJson.getBytes());
            projectManagerEntity.setStatus(Status.NOT_STARTED);
            projectManagerRepository.save(projectManagerEntity);

            Long id = projectManagerEntity.getId();
            String projectName = projectManagerEntity.getProjectName();
            String managerEntityName = projectManagerEntity.getManagerName();

            String hash = HashUtil.sha256(id + projectName + managerEntityName);
            projectManagerEntity.setProjectHash(hash);
            projectManagerRepository.save(projectManagerEntity);

            logger.info("Database seeded with sample project containing baseline JSON.");
        }
    }
}
