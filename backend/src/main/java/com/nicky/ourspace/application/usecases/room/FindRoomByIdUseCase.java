package com.nicky.ourspace.application.usecases.room;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomNotFoundException;
import com.nicky.ourspace.domain.model.Room;
import com.nicky.ourspace.domain.port.RoomPort;

@Service
public class FindRoomByIdUseCase {

    private final RoomPort roomPort;

    public FindRoomByIdUseCase(RoomPort roomPort) {
        this.roomPort = roomPort;
    }

    public Room execute(UUID id) {
        return this.roomPort.findRoomById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room with id " + id + " not found."));
    }
}
