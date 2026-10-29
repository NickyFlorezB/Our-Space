package com.nicky.ourspace.application.usecases.movie;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.port.ImageStoragePort;
import com.nicky.ourspace.domain.port.MoviePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class DeleteMovieByIdUseCase {

    private final MoviePort moviePort;
    private final ImageStoragePort imageStoragePort;
    private final RoomMemberPort roomMemberPort;

    public DeleteMovieByIdUseCase(
            MoviePort moviePort,
            ImageStoragePort imageStoragePort,
            RoomMemberPort roomMemberPort) {
        this.moviePort = moviePort;
        this.imageStoragePort = imageStoragePort;
        this.roomMemberPort = roomMemberPort;
    }

    public void execute(UUID roomId, UUID movieId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can delete movies");
        }
        moviePort.findById(roomId, movieId)
                .filter(movie -> movie.imageUrl() != null)
                .ifPresent(movie -> imageStoragePort.deleteMovieCover(movieId));
        moviePort.deleteById(roomId, movieId);
    }
}
