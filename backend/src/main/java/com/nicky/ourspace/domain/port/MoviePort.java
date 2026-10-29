package com.nicky.ourspace.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nicky.ourspace.domain.model.Movie;

public interface MoviePort {
    Movie save(Movie movie);

    List<Movie> findByRoomId(UUID roomId);

    Optional<Movie> findById(UUID roomId, UUID movieId);

    void deleteById(UUID roomId, UUID movieId);
}
