package com.nicky.ourspace.application.usecases.image;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.port.ImagePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

import com.nicky.ourspace.domain.model.Image;

@Service
public class FindRoomImagesUseCase {

    private final ImagePort imagePort;
    private final RoomMemberPort roomMemberPort;

    public FindRoomImagesUseCase(ImagePort imagePort, RoomMemberPort roomMemberPort) {
        this.imagePort = imagePort;
        this.roomMemberPort = roomMemberPort;
    }

    @Transactional(readOnly = true)
    public List<Image> execute(UUID roomId, UUID userId) {
        requireMembership(roomId, userId);
        return imagePort.findByRoomId(roomId);
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can view room images");
        }
    }
}
