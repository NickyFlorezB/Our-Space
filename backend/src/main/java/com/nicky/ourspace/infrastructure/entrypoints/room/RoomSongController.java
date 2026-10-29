package com.nicky.ourspace.infrastructure.entrypoints.room;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nicky.ourspace.application.usecases.song.DeleteSongByIdUseCase;
import com.nicky.ourspace.application.usecases.song.FindSongByRoomId;
import com.nicky.ourspace.application.usecases.song.SaveSongUseCase;
import com.nicky.ourspace.domain.model.Song;

@RestController
@RequestMapping("/room/{roomId}/songs")
public class RoomSongController {

    private final DeleteSongByIdUseCase deleteSongByIdUseCase;
    private final FindSongByRoomId findSongByRoomId;
    private final SaveSongUseCase saveSongUseCase;

    public RoomSongController(DeleteSongByIdUseCase deleteSongByIdUseCase, FindSongByRoomId findSongByRoomId,
            SaveSongUseCase saveSongUseCase) {
        this.deleteSongByIdUseCase = deleteSongByIdUseCase;
        this.findSongByRoomId = findSongByRoomId;
        this.saveSongUseCase = saveSongUseCase;
    }

    @GetMapping
    public ResponseEntity<List<Song>> findByRoomId(@PathVariable("roomId") UUID roomId,
            Authentication authentication) {
        return ResponseEntity.ok(this.findSongByRoomId.execute(roomId, authenticatedUserId(authentication)));
    }

    @PostMapping
    public ResponseEntity<Song> save(@RequestBody Song song, @PathVariable("roomId") UUID roomId,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.saveSongUseCase.execute(roomId, authenticatedUserId(authentication), song.name(),
                        song.comment(), song.url()));
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> delete(
            @PathVariable("roomId") UUID roomId,
            @PathVariable("songId") UUID songId,
            Authentication authentication) {
        this.deleteSongByIdUseCase.execute(roomId, songId, authenticatedUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}
