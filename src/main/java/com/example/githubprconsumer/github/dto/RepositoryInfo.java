package com.example.githubprconsumer.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RepositoryInfo(@JsonProperty("full_name") String fullName) {
}
