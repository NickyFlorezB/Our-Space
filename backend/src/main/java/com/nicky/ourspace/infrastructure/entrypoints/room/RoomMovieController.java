package com.nicky.ourspace.infrastructure.entrypoints.room;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.nicky.ourspace.application.usecases.movie.DeleteMovieByIdUseCase;
import com.nicky.ourspace.application.usecases.movie.DownloadMovieCoverUseCase;
import com.nicky.ourspace.application.usecases.movie.FindMovieByRoomIdUseCase;
import com.nicky.ourspace.application.usecases.movie.SaveMovieUseCase;
import com.nicky.ourspace.domain.model.ImageUpload;
import com.nicky.ourspace.domain.model.Movie;

@RestController
@RequestMapping("/room/{roomId}/movies")
public class RoomMovieController {

    private final DeleteMovieByIdUseCase deleteMovieByIdUseCase;
    private final DownloadMovieCoverUseCase downloadMovieCoverUseCase;
    private final FindMovieByRoomIdUseCase findMovieByRoomIdUseCase;
    private final SaveMovieUseCase saveMovieUseCase;

    public RoomMovieController(
            DeleteMovieByIdUseCase deleteMovieByIdUseCase,
            DownloadMovieCoverUseCase downloadMovieCoverUseCase,
            FindMovieByRoomIdUseCase findMovieByRoomIdUseCase,
            SaveMovieUseCase saveMovieUseCase) {
        this.deleteMovieByIdUseCase = deleteMovieByIdUseCase;
        this.downloadMovieCoverUseCase = downloadMovieCoverUseCase;
        this.findMovieByRoomIdUseCase = findMovieByRoomIdUseCase;
        this.saveMovieUseCase = saveMovieUseCase;
    }

    @GetMapping
    public ResponseEntity<List<Movie>> findByRoomId(
            @PathVariable("roomId") UUID roomId,
            Authentication authentication) {
        return ResponseEntity.ok(findMovieByRoomIdUseCase.execute(roomId, authenticatedUserId(authentication)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> save(
            @RequestBody Movie movie,
            @PathVariable("roomId") UUID roomId,
            Authentication authentication) {
        return saveMovie(movie, roomId, authenticatedUserId(authentication), null);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Movie> saveWithCover(
            @PathVariable("roomId") UUID roomId,
            @RequestPart("movie") Movie movie,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) throws IOException {
        if (file == null) {
            return saveMovie(movie, roomId, authenticatedUserId(authentication), null);
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("An image file is required");
        }

        try (InputStream content = file.getInputStream()) {
            return saveMovie(movie, roomId, authenticatedUserId(authentication),
                    new ImageUpload(content, file.getSize()));
        }
    }

    @GetMapping("/{movieId}/cover")
    public ResponseEntity<StreamingResponseBody> getCover(
            @PathVariable("roomId") UUID roomId,
            @PathVariable("movieId") UUID movieId,
            Authentication authentication) {
        InputStream content = downloadMovieCoverUseCase.execute(
                roomId, movieId, authenticatedUserId(authentication));
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

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> delete(
            @PathVariable("roomId") UUID roomId,
            @PathVariable("movieId") UUID movieId,
            Authentication authentication) {
        deleteMovieByIdUseCase.execute(roomId, movieId, authenticatedUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Movie> saveMovie(Movie movie, UUID roomId, UUID userId, ImageUpload coverUpload) {
        Movie saved = saveMovieUseCase.execute(
                roomId, userId, movie.name(), movie.comment(), movie.url(), coverUpload);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    private UUID authenticatedUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}
