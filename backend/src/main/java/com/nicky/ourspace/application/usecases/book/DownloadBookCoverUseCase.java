package com.nicky.ourspace.application.usecases.book;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.BookNotFoundException;
import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.port.BookPort;
import com.nicky.ourspace.domain.port.ImageStoragePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class DownloadBookCoverUseCase {

    private final BookPort bookPort;
    private final ImageStoragePort imageStoragePort;
    private final RoomMemberPort roomMemberPort;

    public DownloadBookCoverUseCase(
            BookPort bookPort,
            ImageStoragePort imageStoragePort,
            RoomMemberPort roomMemberPort) {
        this.bookPort = bookPort;
        this.imageStoragePort = imageStoragePort;
        this.roomMemberPort = roomMemberPort;
    }

    public InputStream execute(UUID roomId, UUID bookId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can view book covers");
        }
        var book = bookPort.findById(roomId, bookId)
                .filter(item -> item.imageUrl() != null)
                .orElseThrow(() -> new BookNotFoundException("Book cover not found with id: " + bookId));
        return imageStoragePort.readBookCover(book.id());
    }
}
