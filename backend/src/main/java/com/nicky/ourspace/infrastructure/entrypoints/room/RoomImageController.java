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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.nicky.ourspace.application.dto.ImageDownload;
import com.nicky.ourspace.application.usecases.image.DeleteRoomImageUseCase;
import com.nicky.ourspace.application.usecases.image.DownloadRoomImageUseCase;
import com.nicky.ourspace.application.usecases.image.FindRoomImagesUseCase;
import com.nicky.ourspace.application.usecases.image.UploadRoomImageUseCase;
import com.nicky.ourspace.domain.model.Image;
import com.nicky.ourspace.domain.model.ImageUpload;

@RestController
@RequestMapping("/room/{roomId}/images")
public class RoomImageController {

    private final UploadRoomImageUseCase uploadRoomImageUseCase;
    private final FindRoomImagesUseCase findRoomImagesUseCase;
    private final DownloadRoomImageUseCase downloadRoomImageUseCase;
    private final DeleteRoomImageUseCase deleteRoomImageUseCase;

    public RoomImageController(
            UploadRoomImageUseCase uploadRoomImageUseCase,
            FindRoomImagesUseCase findRoomImagesUseCase,
            DownloadRoomImageUseCase downloadRoomImageUseCase,
            DeleteRoomImageUseCase deleteRoomImageUseCase) {
        this.uploadRoomImageUseCase = uploadRoomImageUseCase;
        this.findRoomImagesUseCase = findRoomImagesUseCase;
        this.downloadRoomImageUseCase = downloadRoomImageUseCase;
        this.deleteRoomImageUseCase = deleteRoomImageUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Image> upload(
            @PathVariable("roomId") UUID roomId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("An image file is required");
        }

        try (InputStream content = file.getInputStream()) {
            Image image = uploadRoomImageUseCase.execute(
                    roomId,
                    authenticatedUserId(authentication),
                    new ImageUpload(content, file.getSize()));
            return ResponseEntity.status(HttpStatus.CREATED).body(image);
        }
    }

    @GetMapping
    public ResponseEntity<List<Image>> findAll(
            @PathVariable("roomId") UUID roomId,
            Authentication authentication) {
        return ResponseEntity.ok(findRoomImagesUseCase.execute(
                roomId,
                authenticatedUserId(authentication)));
    }

    @GetMapping("/{imageId}/content")
    public ResponseEntity<StreamingResponseBody> getContent(
            @PathVariable("roomId") UUID roomId,
            @PathVariable("imageId") UUID imageId,
            Authentication authentication) {
        ImageDownload download = downloadRoomImageUseCase.execute(
                roomId,
                imageId,
                authenticatedUserId(authentication));
        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream content = download.content()) {
                content.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.image().contentType()))
                .contentLength(download.image().size())
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(responseBody);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(
            @PathVariable("roomId") UUID roomId,
            @PathVariable("imageId") UUID imageId,
            Authentication authentication) {
        deleteRoomImageUseCase.execute(roomId, imageId, authenticatedUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}
