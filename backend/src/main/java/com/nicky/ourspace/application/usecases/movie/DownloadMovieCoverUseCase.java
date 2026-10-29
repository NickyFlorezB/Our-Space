package com.nicky.ourspace.application.usecases.movie;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.MovieNotFoundException;
import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.port.ImageStoragePort;
import com.nicky.ourspace.domain.port.MoviePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class DownloadMovieCoverUseCase {

    private final MoviePort moviePort;
    private final ImageStoragePort imageStoragePort;
    private final RoomMemberPort roomMemberPort;

    public DownloadMovieCoverUseCase(
            MoviePort moviePort,
            ImageStoragePort imageStoragePort,
            RoomMemberPort roomMemberPort) {
        this.moviePort = moviePort;
        this.imageStoragePort = imageStoragePort;
        this.roomMemberPort = roomMemberPort;
    }

    public InputStream execute(UUID roomId, UUID movieId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can view movie covers");
        }
        var movie = moviePort.findById(roomId, movieId)
                .filter(item -> item.imageUrl() != null)
                .orElseThrow(() -> new MovieNotFoundException("Movie cover not found with id: " + movieId));
        return imageStoragePort.readMovieCover(movie.id());
    }
}
