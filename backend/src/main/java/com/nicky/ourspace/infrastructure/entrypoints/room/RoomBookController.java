package com.nicky.ourspace.infrastructure.entrypoints.room;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nicky.ourspace.application.usecases.book.DeleteBookByIdUseCase;
import com.nicky.ourspace.application.usecases.book.DownloadBookCoverUseCase;
import com.nicky.ourspace.application.usecases.book.FindBookByRoomIdUseCase;
import com.nicky.ourspace.application.usecases.book.SaveBookUseCase;
import com.nicky.ourspace.domain.model.Book;
import com.nicky.ourspace.domain.model.ImageUpload;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/room/{roomId}/books")
public class RoomBookController {

    private final DeleteBookByIdUseCase deleteBookByIdUseCase;
    private final DownloadBookCoverUseCase downloadBookCoverUseCase;
    private final FindBookByRoomIdUseCase findBookByRoomIdUseCase;
    private final SaveBookUseCase saveBookUseCase;

    public RoomBookController(DeleteBookByIdUseCase deleteBookByIdUseCase,
            DownloadBookCoverUseCase downloadBookCoverUseCase,
            FindBookByRoomIdUseCase findBookByRoomIdUseCase,
            SaveBookUseCase saveBookUseCase) {
        this.deleteBookByIdUseCase = deleteBookByIdUseCase;
        this.downloadBookCoverUseCase = downloadBookCoverUseCase;
        this.findBookByRoomIdUseCase = findBookByRoomIdUseCase;
        this.saveBookUseCase = saveBookUseCase;
    }

    @GetMapping
    public ResponseEntity<List<Book>> findByRoomId(@PathVariable("roomId") UUID roomId,
            Authentication authentication) {
        return ResponseEntity.ok(this.findBookByRoomIdUseCase.execute(roomId, authenticatedUserId(authentication)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Book> save(@RequestBody Book book, @PathVariable("roomId") UUID roomId,
            Authentication authentication) {
        return saveBook(book, roomId, authenticatedUserId(authentication), null);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Book> saveWithCover(
            @PathVariable("roomId") UUID roomId,
            @RequestPart("book") Book book,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) throws IOException {
        if (file == null) {
            return saveBook(book, roomId, authenticatedUserId(authentication), null);
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("An image file is required");
        }

        try (InputStream content = file.getInputStream()) {
            return saveBook(book, roomId, authenticatedUserId(authentication),
                    new ImageUpload(content, file.getSize()));
        }
    }

    @GetMapping("/{bookId}/cover")
    public ResponseEntity<StreamingResponseBody> getCover(
            @PathVariable("roomId") UUID roomId,
            @PathVariable("bookId") UUID bookId,
            Authentication authentication) {
        InputStream content = downloadBookCoverUseCase.execute(
                roomId, bookId, authenticatedUserId(authentication));
        StreamingResponseBody responseBody = outputStream -> {
            try (content) {
                content.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(responseBody);
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

    private ResponseEntity<Book> saveBook(Book book, UUID roomId, UUID userId, ImageUpload coverUpload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saveBookUseCase.execute(roomId, userId, book.isbn_13(),
                        book.title(), book.weight(), book.fullTitle(), book.latestRevision(), book.createdAt(),
                        book.publishers(), coverUpload));
    }
}
