package com.nicky.ourspace.infrastructure.entrypoints.room;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nicky.ourspace.application.usecases.book.DeleteBookByIdUseCase;
import com.nicky.ourspace.application.usecases.book.FindBookByRoomIdUseCase;
import com.nicky.ourspace.application.usecases.book.SaveBookUseCase;
import com.nicky.ourspace.domain.model.Book;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/room/{roomId}/books")
public class RoomBookController {

    private final DeleteBookByIdUseCase deleteBookByIdUseCase;
    private final FindBookByRoomIdUseCase findBookByRoomIdUseCase;
    private final SaveBookUseCase saveBookUseCase;

    public RoomBookController(DeleteBookByIdUseCase deleteBookByIdUseCase,
            FindBookByRoomIdUseCase findBookByRoomIdUseCase, SaveBookUseCase saveBookUseCase) {
        this.deleteBookByIdUseCase = deleteBookByIdUseCase;
        this.findBookByRoomIdUseCase = findBookByRoomIdUseCase;
        this.saveBookUseCase = saveBookUseCase;
    }

    @GetMapping
    public ResponseEntity<List<Book>> findByRoomId(@PathVariable("roomId") UUID roomId,
            Authentication authentication) {
        return ResponseEntity.ok(this.findBookByRoomIdUseCase.execute(roomId, authenticatedUserId(authentication)));
    }

    @PostMapping
    public ResponseEntity<Book> save(@RequestBody Book book, @PathVariable("roomId") UUID roomId,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.saveBookUseCase.execute(roomId, authenticatedUserId(authentication), book.isbn_13(),
                        book.title(), book.weight(), book.fullTitle(), book.latestRevision(), book.createdAt(),
                        book.publishers()));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> delete(
            @PathVariable("roomId") UUID roomId,
            @PathVariable("bookId") UUID bookId,
            Authentication authentication) {
        this.deleteBookByIdUseCase.execute(roomId, bookId, authenticatedUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}
