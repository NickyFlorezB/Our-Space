package com.nicky.ourspace.infrastructure.entrypoints.room;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nicky.ourspace.application.usecases.room.FindRoomByIdUseCase;
import com.nicky.ourspace.application.usecases.room.FindRoomsByOwnerIdUseCase;
import com.nicky.ourspace.application.usecases.room.SaveRoomUseCase;
import com.nicky.ourspace.domain.model.Room;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("room")
public class RoomController {

    private final FindRoomByIdUseCase findRoomByIdUseCase;
    private final FindRoomsByOwnerIdUseCase findRoomsByOwnerIdUseCase;
    private final SaveRoomUseCase saveRoomUseCase;

    public RoomController(FindRoomByIdUseCase findRoomByIdUseCase, FindRoomsByOwnerIdUseCase findRoomsByOwnerIdUseCase,
            SaveRoomUseCase saveRoomUseCase) {
        this.findRoomByIdUseCase = findRoomByIdUseCase;
        this.findRoomsByOwnerIdUseCase = findRoomsByOwnerIdUseCase;
        this.saveRoomUseCase = saveRoomUseCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(this.findRoomByIdUseCase.execute(id));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Room>> findByOwnerId(@PathVariable("ownerId") UUID ownerId) {
        return ResponseEntity.ok(this.findRoomsByOwnerIdUseCase.execute(ownerId));
    }

    @PostMapping("")
    public ResponseEntity<Void> postMethodName(@RequestBody Room room) {
        this.saveRoomUseCase.execute(room.name(), room.description(), room.imageUrl(), room.ownerId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
