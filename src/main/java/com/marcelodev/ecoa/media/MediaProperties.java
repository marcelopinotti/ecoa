package com.marcelodev.ecoa.media;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.media")
public record MediaProperties(Path directory, String espeakCommand) {
}
