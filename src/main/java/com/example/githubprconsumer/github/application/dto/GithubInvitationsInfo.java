package com.example.githubprconsumer.github.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubInvitationsInfo(
        Integer id,
        @JsonProperty("repository") GithubRepositoryInfo githubRepositoryInfo,
        String permissions,
        @JsonProperty("inviter") InviterInfo inviterInfo
) {
}
