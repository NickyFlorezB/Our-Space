package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.RoomNotFoundException;
import com.nicky.ourspace.domain.model.Movie;
import com.nicky.ourspace.domain.port.MoviePort;
import com.nicky.ourspace.infrastructure.drivenadapters.mapper.MovieMapper;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaMovieRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomRepository;

@Service
public class MovieAdapter implements MoviePort {

    private final JpaMovieRepository jpaMovieRepository;
    private final JpaRoomRepository jpaRoomRepository;
    private final MovieMapper movieMapper;

    public MovieAdapter(
            JpaMovieRepository jpaMovieRepository,
            JpaRoomRepository jpaRoomRepository,
            MovieMapper movieMapper) {
        this.jpaMovieRepository = jpaMovieRepository;
        this.jpaRoomRepository = jpaRoomRepository;
        this.movieMapper = movieMapper;
    }

    @Override
    @Transactional
    public Movie save(Movie movie) {
        RoomDbo room = jpaRoomRepository.findById(movie.roomId())
                .orElseThrow(() -> new RoomNotFoundException("Room not found with id: " + movie.roomId()));
        return movieMapper.toDomain(jpaMovieRepository.save(movieMapper.toDbo(movie, room)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movie> findByRoomId(UUID roomId) {
        return jpaMovieRepository.findAllByRoom_IdOrderByIdDesc(roomId).stream()
                .map(movieMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Movie> findById(UUID roomId, UUID movieId) {
        return jpaMovieRepository.findByIdAndRoom_Id(movieId, roomId)
                .map(movieMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(UUID roomId, UUID movieId) {
        jpaMovieRepository.deleteByIdAndRoom_Id(movieId, roomId);
    }
}
