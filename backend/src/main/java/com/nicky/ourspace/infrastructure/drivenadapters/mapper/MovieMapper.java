package com.nicky.ourspace.infrastructure.drivenadapters.mapper;

import org.springframework.stereotype.Component;

import com.nicky.ourspace.domain.model.Movie;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.MovieDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;

@Component
public class MovieMapper {

    public Movie toDomain(MovieDbo dbo) {
        return new Movie(
                dbo.getId(),
                dbo.getRoom().getId(),
                dbo.getName(),
                dbo.getComment(),
                dbo.getUrl(),
                dbo.getImageUrl());
    }

    public MovieDbo toDbo(Movie movie, RoomDbo room) {
        return new MovieDbo(
                movie.id(),
                room,
                movie.name(),
                movie.comment(),
                movie.url(),
                movie.imageUrl());
    }
}
