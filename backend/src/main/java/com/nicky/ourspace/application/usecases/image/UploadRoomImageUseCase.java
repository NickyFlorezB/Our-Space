package com.nicky.ourspace.application.usecases.image;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.Image;
import com.nicky.ourspace.domain.model.ImageUpload;
import com.nicky.ourspace.domain.model.StoredImage;
import com.nicky.ourspace.domain.port.ImagePort;
import com.nicky.ourspace.domain.port.ImageStoragePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class UploadRoomImageUseCase {

    private final ImagePort imagePort;
    private final ImageStoragePort imageStoragePort;
    private final RoomMemberPort roomMemberPort;
    private final String publicPathPrefix;

    public UploadRoomImageUseCase(
            ImagePort imagePort,
            ImageStoragePort imageStoragePort,
            RoomMemberPort roomMemberPort,
            @Value("${app.images.url-prefix:/api/v1}") String publicPathPrefix) {
        this.imagePort = imagePort;
        this.imageStoragePort = imageStoragePort;
        this.roomMemberPort = roomMemberPort;
        this.publicPathPrefix = normalizePrefix(publicPathPrefix);
    }

    public Image execute(UUID roomId, UUID userId, ImageUpload upload) {
        requireMembership(roomId, userId);

        UUID imageId = UUID.randomUUID();
        StoredImage storedImage = imageStoragePort.saveRoomImage(roomId, imageId, upload);
        String url = publicPathPrefix + "/room/" + roomId + "/images/" + imageId + "/content";
        Image image = new Image(imageId, roomId, url, storedImage.contentType(), storedImage.size());

        try {
            return imagePort.save(image);
        } catch (RuntimeException exception) {
            try {
                imageStoragePort.deleteRoomImage(roomId, imageId);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can upload images");
        }
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
