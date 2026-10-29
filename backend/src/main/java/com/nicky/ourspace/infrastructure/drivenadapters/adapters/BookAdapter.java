package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.RoomNotFoundException;
import com.nicky.ourspace.domain.model.Book;
import com.nicky.ourspace.domain.port.BookPort;
import com.nicky.ourspace.infrastructure.drivenadapters.mapper.BookMapper;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaBookRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomRepository;

@Service
public class BookAdapter implements BookPort {

    private final JpaBookRepository jpaBookRepository;
    private final JpaRoomRepository jpaRoomRepository;
    private final BookMapper bookMapper;

    public BookAdapter(JpaBookRepository jpaBookRepository, JpaRoomRepository jpaRoomRepository,
            BookMapper bookMapper) {
        this.jpaBookRepository = jpaBookRepository;
        this.jpaRoomRepository = jpaRoomRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    @Transactional
    public Book save(Book book) {
        RoomDbo room = jpaRoomRepository.findById(book.roomId())
                .orElseThrow(() -> new RoomNotFoundException(
                        "Room not found with id: " + book.roomId()));

        return bookMapper.toDomain(jpaBookRepository.save(bookMapper.toDbo(book, room)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findByRoomId(UUID roomId) {
        return jpaBookRepository.findAllByRoom_IdOrderByIdDesc(roomId).stream()
                .map(bookMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findById(UUID roomId, UUID bookId) {
        return jpaBookRepository.findByIdAndRoom_Id(bookId, roomId)
                .map(bookMapper::toDomain);
    }

    @Override
    public void deleteById(UUID roomId, UUID bookId) {
        jpaBookRepository.deleteByIdAndRoom_Id(bookId, roomId);
    }

}
