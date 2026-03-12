package com.devpulse.webhook.controller;

import com.devpulse.webhook.service.SignatureVerifier;
import jakarta.servlet.http.HttpServletRequest;
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

        log.info("Pull request event received");
        log.info("Payload preview: {}", payload.substring(0, Math.min(200, payload.length())));

        return ResponseEntity.ok("Webhook received");
    }
}