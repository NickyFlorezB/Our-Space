package com.nicky.ourspace.application.usecases.book;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.port.BookPort;
import com.nicky.ourspace.domain.port.ImageStoragePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class DeleteBookByIdUseCase {

    private final BookPort bookPort;
    private final ImageStoragePort imageStoragePort;
    private final RoomMemberPort roomMemberPort;

    public DeleteBookByIdUseCase(
            BookPort bookPort,
            ImageStoragePort imageStoragePort,
            RoomMemberPort roomMemberPort) {
        this.bookPort = bookPort;
        this.imageStoragePort = imageStoragePort;
        this.roomMemberPort = roomMemberPort;
    }

    public void execute(UUID roomId, UUID bookId, UUID userId) {
        this.requireMembership(roomId, userId);
        bookPort.findById(roomId, bookId)
                .filter(book -> book.imageUrl() != null)
                .ifPresent(book -> imageStoragePort.deleteBookCover(bookId));
        this.bookPort.deleteById(roomId, bookId);
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can delete books");
        }
    }
}
