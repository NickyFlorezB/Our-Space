package com.nicky.ourspace.application.usecases.movie;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.nicky.ourspace.application.events.RoomActivityCreatedEvent;
import com.nicky.ourspace.application.usecases.image.ImageUrlFactory;
import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.ImageUpload;
import com.nicky.ourspace.domain.model.Movie;
import com.nicky.ourspace.domain.port.ImageStoragePort;
import com.nicky.ourspace.domain.port.MoviePort;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class SaveMovieUseCase {

    private final MoviePort moviePort;
    private final ImageStoragePort imageStoragePort;
    private final RoomMemberPort roomMemberPort;
    private final ImageUrlFactory imageUrlFactory;
    private final ApplicationEventPublisher eventPublisher;

    public SaveMovieUseCase(
            MoviePort moviePort,
            ImageStoragePort imageStoragePort,
            RoomMemberPort roomMemberPort,
            ImageUrlFactory imageUrlFactory,
            ApplicationEventPublisher eventPublisher) {
        this.moviePort = moviePort;
        this.imageStoragePort = imageStoragePort;
        this.roomMemberPort = roomMemberPort;
        this.imageUrlFactory = imageUrlFactory;
        this.eventPublisher = eventPublisher;
    }

    public Movie execute(UUID roomId, UUID userId, String name, String comment, String url, ImageUpload coverUpload) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can add movies");
        }

        UUID movieId = UUID.randomUUID();
        boolean coverStored = false;
        String imageUrl = null;
        if (coverUpload != null) {
            imageStoragePort.saveMovieCover(movieId, coverUpload);
            coverStored = true;
            imageUrl = imageUrlFactory.movieCover(roomId, movieId);
        }

        try {
            Movie savedMovie = moviePort.save(new Movie(movieId, roomId, name, comment, url, imageUrl));
            eventPublisher.publishEvent(new RoomActivityCreatedEvent(
                    roomId, userId, "Nueva película", savedMovie.name(), "/movies?roomId=" + roomId));
            return savedMovie;
        } catch (RuntimeException exception) {
            if (coverStored) {
                try {
                    imageStoragePort.deleteMovieCover(movieId);
                } catch (RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw exception;
        }
    }
}
