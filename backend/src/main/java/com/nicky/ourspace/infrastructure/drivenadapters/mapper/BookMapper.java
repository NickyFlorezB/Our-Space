package com.nicky.ourspace.infrastructure.drivenadapters.mapper;

import org.springframework.stereotype.Component;

import com.nicky.ourspace.domain.model.Book;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.BookDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;

@Component
public class BookMapper {

    public Book toDomain(BookDbo dbo) {
        return new Book(
                dbo.getId(),
                dbo.getRoom().getId(),
                dbo.getIsbn_13(),
                dbo.getTitle(),
                dbo.getWeight(),
                dbo.getFullTitle(),
                dbo.getLatestRevision(),
                dbo.getCreatedAt(),
                dbo.getPublishers(),
                dbo.getImageUrl());
    }

    public BookDbo toDbo(Book book, RoomDbo room) {
        return new BookDbo(
                book.id(),
                room,
                book.isbn_13(),
                book.title(),
                book.weight(),
                book.fullTitle(),
                book.latestRevision(),
                book.createdAt(),
                book.publishers(),
                book.imageUrl());
    }
}
