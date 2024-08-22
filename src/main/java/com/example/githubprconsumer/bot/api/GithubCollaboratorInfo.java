package com.example.githubprconsumer.bot.api;

import com.example.githubprconsumer.collaborator.Collaborator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubCollaboratorInfo(
        @JsonProperty("login") String login,
        @JsonProperty("avatar_url") String avatarUrl,
        @JsonProperty("html_url") String htmlUrl
) {
    public Collaborator toEntity(Long repositoryId){
        return new Collaborator(
                repositoryId,
                login,
                avatarUrl,
                htmlUrl
        );
    }
}