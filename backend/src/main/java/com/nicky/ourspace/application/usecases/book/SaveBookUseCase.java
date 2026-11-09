package com.nicky.ourspace.application.usecases.book;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.nicky.ourspace.application.events.RoomActivityCreatedEvent;
import com.nicky.ourspace.application.usecases.image.ImageUrlFactory;
import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.Book;
import com.nicky.ourspace.domain.model.ImageUpload;
import com.nicky.ourspace.domain.port.BookPort;
import com.nicky.ourspace.domain.port.ImageStoragePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class SaveBookUseCase {

    private final BookPort bookPort;
    private final ImageStoragePort imageStoragePort;
    private final RoomMemberPort roomMemberPort;
    private final ImageUrlFactory imageUrlFactory;
    private final ApplicationEventPublisher eventPublisher;

    public SaveBookUseCase(
            BookPort bookPort,
            ImageStoragePort imageStoragePort,
            RoomMemberPort roomMemberPort,
            ImageUrlFactory imageUrlFactory,
            ApplicationEventPublisher eventPublisher) {
        this.bookPort = bookPort;
        this.imageStoragePort = imageStoragePort;
        this.roomMemberPort = roomMemberPort;
        this.imageUrlFactory = imageUrlFactory;
        this.eventPublisher = eventPublisher;
    }

    public Book execute(UUID roomId, UUID userId, String isbn_13,
            String title,
            String weight,
            String fullTitle,
            Integer latestRevision,
            String createdAt,
            List<String> publishers,
            ImageUpload coverUpload) {
        this.requireMembership(roomId, userId);
        UUID bookId = UUID.randomUUID();
        boolean coverStored = false;
        String imageUrl = null;
        if (coverUpload != null) {
            imageStoragePort.saveBookCover(bookId, coverUpload);
            coverStored = true;
            imageUrl = imageUrlFactory.bookCover(roomId, bookId);
        }

        try {
            Book savedBook = bookPort.save(new Book(bookId, roomId, isbn_13, title, weight, fullTitle,
                    latestRevision, createdAt, publishers, imageUrl));
            eventPublisher.publishEvent(new RoomActivityCreatedEvent(
                    roomId, userId, "Nuevo libro", savedBook.title(), "/books?roomId=" + roomId));
            return savedBook;
        } catch (RuntimeException exception) {
            if (coverStored) {
                try {
                    imageStoragePort.deleteBookCover(bookId);
                } catch (RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw exception;
        }
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can add books");
        }
    }
}
