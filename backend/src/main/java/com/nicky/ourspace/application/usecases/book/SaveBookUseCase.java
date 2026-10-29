package com.nicky.ourspace.application.usecases.book;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.Book;
import com.nicky.ourspace.domain.port.BookPort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class SaveBookUseCase {

    private final BookPort bookPort;
    private final RoomMemberPort roomMemberPort;

    public SaveBookUseCase(BookPort bookPort, RoomMemberPort roomMemberPort) {
        this.bookPort = bookPort;
        this.roomMemberPort = roomMemberPort;
    }

    public Book execute(UUID roomId, UUID userId, String isbn_13,
            String title,
            String weight,
            String fullTitle,
            Integer latestRevision,
            String createdAt,
            List<String> publishers) {
        this.requireMembership(roomId, userId);
        return this.bookPort.save(new Book(UUID.randomUUID(), roomId, isbn_13, title, weight, fullTitle, latestRevision,
                createdAt, publishers));
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can upload images");
        }
    }
}
