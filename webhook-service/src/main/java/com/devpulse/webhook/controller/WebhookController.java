package com.devpulse.webhook.controller;

import com.devpulse.webhook.model.PullRequestEvent;
import com.devpulse.webhook.service.KafkaProducerService;
import com.devpulse.webhook.service.SignatureVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/github")
public class WebhookController {

    private final SignatureVerifier signatureVerifier;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestBody String payload) {

        log.info("Received GitHub event: {}", eventType);

        if (!signatureVerifier.isValid(payload, signature)) {
            log.warn("Invalid signature — request rejected");
            return ResponseEntity.status(401).body("Invalid signature");
        }

        log.info("Signature valid");

        if (!eventType.equals("pull_request")) {
            log.info("Ignoring event type: {}", eventType);
            return ResponseEntity.ok("Event ignored");
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String action = root.path("action").asText();

            if (!action.equals("opened") && !action.equals("synchronize")) {
                log.info("Ignoring PR action: {}", action);
                return ResponseEntity.ok("Action ignored");
            }

            int prNumber = root.path("number").asInt();
            String repoFullName = root.path("repository").path("full_name").asText();
            long installationId = root.path("installation").path("id").asLong();

            log.info("PR #{} {} on {}", prNumber, action, repoFullName);

            PullRequestEvent event = new PullRequestEvent(prNumber, repoFullName, action, installationId);
            kafkaProducerService.publishPREvent(event);

        } catch (Exception e) {
            log.error("Failed to process webhook payload", e);
            return ResponseEntity.status(500).body("Processing error");
        }

        return ResponseEntity.ok("Webhook received");
    }
}