package com.nicky.ourspace.application.usecases.movie;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.Movie;
import com.nicky.ourspace.domain.port.MoviePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class FindMovieByRoomIdUseCase {

    private final MoviePort moviePort;
    private final RoomMemberPort roomMemberPort;

    public FindMovieByRoomIdUseCase(MoviePort moviePort, RoomMemberPort roomMemberPort) {
        this.moviePort = moviePort;
        this.roomMemberPort = roomMemberPort;
    }

    public List<Movie> execute(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can view movies");
        }
        return moviePort.findByRoomId(roomId);
    }
}
