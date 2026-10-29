package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.RoomNotFoundException;
import com.nicky.ourspace.domain.model.Image;
import com.nicky.ourspace.domain.port.ImagePort;
import com.nicky.ourspace.infrastructure.drivenadapters.mapper.ImageMapper;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaImageRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomRepository;

@Service
public class ImageAdapter implements ImagePort {

    private final JpaImageRepository jpaImageRepository;
    private final JpaRoomRepository jpaRoomRepository;
    private final ImageMapper imageMapper;

    public ImageAdapter(
            JpaImageRepository jpaImageRepository,
            JpaRoomRepository jpaRoomRepository,
            ImageMapper imageMapper) {
        this.jpaImageRepository = jpaImageRepository;
        this.jpaRoomRepository = jpaRoomRepository;
        this.imageMapper = imageMapper;
    }

    @Override
    @Transactional
    public Image save(Image image) {
        RoomDbo room = jpaRoomRepository.findById(image.roomId())
                .orElseThrow(() -> new RoomNotFoundException(
                        "Room not found with id: " + image.roomId()));
        return imageMapper.toDomain(jpaImageRepository.save(imageMapper.toDbo(image, room)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Image> findByRoomId(UUID roomId) {
        return jpaImageRepository.findAllByRoom_IdOrderByIdDesc(roomId).stream()
                .map(imageMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Image> findById(UUID roomId, UUID imageId) {
        return jpaImageRepository.findByIdAndRoom_Id(imageId, roomId)
                .map(imageMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(UUID roomId, UUID imageId) {
        jpaImageRepository.deleteByIdAndRoom_Id(imageId, roomId);
    }
}
