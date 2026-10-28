package com.nicky.ourspace.application.usecases.image;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.ImageNotFoundException;
import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.RoomMemberRole;
import com.nicky.ourspace.domain.port.ImagePort;
import com.nicky.ourspace.domain.port.ImageStoragePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class DeleteRoomImageUseCase {

    private final ImagePort imagePort;
    private final ImageStoragePort imageStoragePort;
    private final RoomMemberPort roomMemberPort;

    public DeleteRoomImageUseCase(
            ImagePort imagePort,
            ImageStoragePort imageStoragePort,
            RoomMemberPort roomMemberPort) {
        this.imagePort = imagePort;
        this.imageStoragePort = imageStoragePort;
        this.roomMemberPort = roomMemberPort;
    }

    public void execute(UUID roomId, UUID imageId, UUID userId) {
        var membership = roomMemberPort.findRoomMember(roomId, userId)
                .orElseThrow(() -> new RoomMemberAccessDeniedException(
                        "Only room owners can delete room images"));
        if (membership.role() != RoomMemberRole.OWNER) {
            throw new RoomMemberAccessDeniedException("Only room owners can delete room images");
        }

        imagePort.findById(roomId, imageId)
                .orElseThrow(() -> new ImageNotFoundException(
                        "Image not found with id: " + imageId));

        imageStoragePort.deleteRoomImage(roomId, imageId);
        imagePort.deleteById(roomId, imageId);
    }
}
