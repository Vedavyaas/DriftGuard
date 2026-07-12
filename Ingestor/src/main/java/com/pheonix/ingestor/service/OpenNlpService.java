package com.pheonix.ingestor.service;

import com.pheonix.ingestor.dto.DriftEventDTO;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class OpenNlpService {

    private TokenizerME tokenizer;
    private POSTaggerME posTagger;

    public OpenNlpService() {
        try {
            // Load models from classpath
            try (InputStream tokenStream = new ClassPathResource("opennlp/en-token.bin").getInputStream()) {
                TokenizerModel tokenModel = new TokenizerModel(tokenStream);
                this.tokenizer = new TokenizerME(tokenModel);
            }

            try (InputStream posStream = new ClassPathResource("opennlp/en-pos-maxent.bin").getInputStream()) {
                POSModel posModel = new POSModel(posStream);
                this.posTagger = new POSTaggerME(posModel);
            }
        } catch (Exception e) {
            System.err.println("Failed to load OpenNLP models: " + e.getMessage());
        }
    }

    public DriftEventDTO parseNaturalLanguage(String text) {
        DriftEventDTO event = new DriftEventDTO();
        event.setEventId("NLP-" + UUID.randomUUID().toString().substring(0, 8));
        event.setTimestamp(java.time.Instant.now().toString());
        event.setChangeSource("nlp_extracted");
        event.setApprovalStatus("pending");
        event.setMaintenanceWindow(false);

        // Fallback generic values
        event.setControlId("CTRL-NLP-GENERIC");
        event.setParameter("nlp_inferred_rule");
        event.setOldValue("baseline_secure");
        event.setNewValue("nlp_drift_detected");
        event.setSystem("nlp-identified-system");
        event.setEnvironment("production");
        event.setDomain("Unknown");
        event.setSeverity("LOW");
        event.setChangedBy("unknown_actor");

        if (tokenizer == null || posTagger == null) {
            System.err.println("OpenNLP models not loaded correctly, falling back to basic extraction.");
            event.setProjectHash("FALLBACK-NLP-HASH");
            return event;
        }

        String[] tokens = tokenizer.tokenize(text);
        String[] tags = posTagger.tag(tokens);
        List<String> tokenList = Arrays.asList(tokens);

        // 1. Extract Project Hash (Look for UUIDs or long alphanumeric strings)
        for (String t : tokenList) {
            if (t.length() >= 32 && t.matches("^[a-fA-F0-9-]+$")) {
                event.setProjectHash(t);
                break;
            }
        }

        // 2. Extract Project Hash explicitly if preceded by "project" or "hash"
        if (event.getProjectHash() == null) {
            for (int i = 0; i < tokenList.size() - 1; i++) {
                String t = tokenList.get(i).toLowerCase();
                if (t.equals("project") || t.equals("hash")) {
                    String next = tokenList.get(i + 1);
                    if (!next.equals(":") && !next.equals("=")) {
                        event.setProjectHash(next);
                        break;
                    } else if (i + 2 < tokenList.size()) {
                        event.setProjectHash(tokenList.get(i + 2));
                        break;
                    }
                }
            }
        }
        
        if (event.getProjectHash() == null) {
            event.setProjectHash("FALLBACK-NLP-HASH");
        }

        // 3. Extract Domains and Actions using Grammar (POS Tags) + Dictionaries
        boolean foundAction = false;
        
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            String tag = tags[i];
            String lowerToken = token.toLowerCase();

            // Detect Nouns (NN, NNP, NNS, NNPS) for Domain/Actor
            if (tag.startsWith("NN") || tag.startsWith("FW") || tag.startsWith("JJ")) { // FW=Foreign word, JJ=Adjective (often misclassified tech terms)
                if (lowerToken.contains("aws") || lowerToken.contains("ec2") || lowerToken.contains("s3")) event.setDomain("AWS");
                else if (lowerToken.contains("github") || lowerToken.contains("repo")) event.setDomain("GitHub");
                else if (lowerToken.contains("gcp") || lowerToken.contains("google")) event.setDomain("GCP");
                else if (lowerToken.contains("azure")) event.setDomain("Azure");
                else if (lowerToken.contains("k8s") || lowerToken.contains("kubernetes") || lowerToken.contains("pod")) event.setDomain("Kubernetes");
                else if (lowerToken.contains("okta") || lowerToken.contains("iam") || lowerToken.contains("identity")) event.setDomain("Okta");
                else if (lowerToken.contains("slack")) event.setDomain("Slack");
                else if (lowerToken.contains("database") || lowerToken.contains("sql") || lowerToken.contains("postgres")) event.setDomain("Database");
                else if (lowerToken.contains("network") || lowerToken.contains("firewall") || lowerToken.contains("port") || lowerToken.contains("sg")) event.setDomain("Network");

                if (lowerToken.contains("admin")) event.setChangedBy("admin");
                else if (lowerToken.contains("root")) event.setChangedBy("root");
                else if (lowerToken.contains("user")) event.setChangedBy("user");
                else if (lowerToken.contains("bot") || lowerToken.contains("script")) event.setChangedBy("bot");
            }

            // Detect Verbs (VB, VBD, VBG, VBN, VBP, VBZ) for Action/Severity
            if (tag.startsWith("VB")) {
                if (lowerToken.contains("delete") || lowerToken.contains("remove") || lowerToken.contains("disable") || lowerToken.contains("drop")) {
                    event.setSeverity("CRITICAL");
                    foundAction = true;
                } else if (lowerToken.contains("open") || lowerToken.contains("public") || lowerToken.contains("expose")) {
                    if (!foundAction || event.getSeverity().equals("LOW")) {
                        event.setSeverity("HIGH");
                        foundAction = true;
                    }
                } else if (lowerToken.contains("modify") || lowerToken.contains("change") || lowerToken.contains("update")) {
                    if (!foundAction || event.getSeverity().equals("LOW")) {
                        event.setSeverity("MEDIUM");
                        foundAction = true;
                    }
                }
            }
        }

        return event;
    }
}
