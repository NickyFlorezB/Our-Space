package com.nicky.ourspace.application.usecases.book;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.Book;
import com.nicky.ourspace.domain.port.BookPort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class FindBookByRoomIdUseCase {

    private final BookPort bookPort;
    private final RoomMemberPort roomMemberPort;

    public FindBookByRoomIdUseCase(BookPort bookPort, RoomMemberPort roomMemberPort) {
        this.bookPort = bookPort;
        this.roomMemberPort = roomMemberPort;
    }

    public List<Book> execute(UUID roomId, UUID userId) {
        this.requireMembership(roomId, userId);
        return this.bookPort.findByRoomId(roomId);
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can upload images");
        }
    }

}
