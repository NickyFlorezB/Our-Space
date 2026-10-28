package com.nicky.ourspace.application.usecases.image;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.application.dto.ImageDownload;
import com.nicky.ourspace.domain.exception.ImageNotFoundException;
import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.port.ImagePort;
import com.nicky.ourspace.domain.port.ImageStoragePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class DownloadRoomImageUseCase {

    private final ImagePort imagePort;
    private final ImageStoragePort imageStoragePort;
    private final RoomMemberPort roomMemberPort;

    public DownloadRoomImageUseCase(
            ImagePort imagePort,
            ImageStoragePort imageStoragePort,
            RoomMemberPort roomMemberPort) {
        this.imagePort = imagePort;
        this.imageStoragePort = imageStoragePort;
        this.roomMemberPort = roomMemberPort;
    }

    public ImageDownload execute(UUID roomId, UUID imageId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can view room images");
        }

        var image = imagePort.findById(roomId, imageId)
                .orElseThrow(() -> new ImageNotFoundException(
                        "Image not found with id: " + imageId));

        return new ImageDownload(image, imageStoragePort.readRoomImage(roomId, imageId));
    }
}
