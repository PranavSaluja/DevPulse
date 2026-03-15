package com.devpulse.webhook.service;

import com.devpulse.webhook.model.PullRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, PullRequestEvent> kafkaTemplate;

    @Value("${devpulse.kafka.topic}")
    private String topic;

    public void publishPREvent(PullRequestEvent event) {
        kafkaTemplate.send(topic, event);
        log.info("Published PR event to Kafka — PR #{} on {}",
                event.getPrNumber(), event.getRepoFullName());
    }
}