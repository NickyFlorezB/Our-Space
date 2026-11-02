package com.nicky.ourspace.application.usecases.image;

import java.util.UUID;

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
    private final ImageUrlFactory imageUrlFactory;

    public UploadRoomImageUseCase(
            ImagePort imagePort,
            ImageStoragePort imageStoragePort,
            RoomMemberPort roomMemberPort,
            ImageUrlFactory imageUrlFactory) {
        this.imagePort = imagePort;
        this.imageStoragePort = imageStoragePort;
        this.roomMemberPort = roomMemberPort;
        this.imageUrlFactory = imageUrlFactory;
    }

    public Image execute(UUID roomId, UUID userId, ImageUpload upload, String title, String comment) {
        requireMembership(roomId, userId);
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Image title is required");
        }
        if (title.length() > 160) {
            throw new IllegalArgumentException("Image titles cannot exceed 160 characters");
        }
        if (comment != null && comment.length() > 1000) {
            throw new IllegalArgumentException("Image comments cannot exceed 1000 characters");
        }

        UUID imageId = UUID.randomUUID();
        StoredImage storedImage = imageStoragePort.saveRoomImage(roomId, imageId, upload);
        String url = imageUrlFactory.roomImage(roomId, imageId);
        Image image = new Image(
                imageId,
                roomId,
                url,
                storedImage.contentType(),
                storedImage.size(),
                title.trim(),
                comment == null || comment.isBlank() ? null : comment.trim());

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

}
