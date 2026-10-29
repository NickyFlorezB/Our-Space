package com.nicky.ourspace.infrastructure.drivenadapters.mapper;

import org.springframework.stereotype.Component;

import com.nicky.ourspace.domain.model.Image;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.ImageDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;

@Component
public class ImageMapper {

    public Image toDomain(ImageDbo dbo) {
        return new Image(
                dbo.getId(),
                dbo.getRoom().getId(),
                dbo.getUrl(),
                dbo.getContentType(),
                dbo.getSize());
    }

    public ImageDbo toDbo(Image image, RoomDbo room) {
        return new ImageDbo(
                image.id(),
                room,
                image.url(),
                image.contentType(),
                image.size());
    }
}
