package com.devpulse.webhook.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PullRequestEvent {
    private int prNumber;
    private String repoFullName;
    private String action;
    private long installationId;
}