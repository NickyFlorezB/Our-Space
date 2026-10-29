package com.nicky.ourspace.application.usecases.book;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.port.BookPort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class DeleteBookByIdUseCase {

    private final BookPort bookPort;
    private final RoomMemberPort roomMemberPort;

    public DeleteBookByIdUseCase(BookPort bookPort, RoomMemberPort roomMemberPort) {
        this.bookPort = bookPort;
        this.roomMemberPort = roomMemberPort;
    }

    public void execute(UUID roomId, UUID bookId, UUID userId) {
        this.requireMembership(roomId, userId);
        this.bookPort.deleteById(roomId, bookId);
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can upload images");
        }
    }
}
