package com.nicky.ourspace.infrastructure.drivenadapters.storage;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record ImageStorageProperties(
        Path root) {
}