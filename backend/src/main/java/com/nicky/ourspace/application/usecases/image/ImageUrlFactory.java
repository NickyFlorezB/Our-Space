package com.nicky.ourspace.application.usecases.image;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlFactory {

    private final String publicPathPrefix;

    public ImageUrlFactory(@Value("${app.images.url-prefix:/api/v1}") String publicPathPrefix) {
        this.publicPathPrefix = normalizePrefix(publicPathPrefix);
    }

    public String roomImage(UUID roomId, UUID imageId) {
        return publicPathPrefix + "/room/" + roomId + "/images/" + imageId + "/content";
    }

    public String movieCover(UUID roomId, UUID movieId) {
        return publicPathPrefix + "/room/" + roomId + "/movies/" + movieId + "/cover";
    }

    public String bookCover(UUID roomId, UUID bookId) {
        return publicPathPrefix + "/room/" + roomId + "/books/" + bookId + "/cover";
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank() || prefix.equals("/")) {
            return "";
        }
        String normalized = prefix.startsWith("/") ? prefix : "/" + prefix;
        return normalized.endsWith("/")
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }
}
