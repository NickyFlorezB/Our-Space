package com.nicky.ourspace.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nicky.ourspace.domain.model.Book;

public interface BookPort {
    Book save(Book book);

    List<Book> findByRoomId(UUID roomId);

    Optional<Book> findById(UUID roomId, UUID bookId);

    void deleteById(UUID roomId, UUID bookId);
}
