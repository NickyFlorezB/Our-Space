package com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "images")
public class ImageDbo {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomDbo room;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long size;

    protected ImageDbo() {
    }

    public ImageDbo(UUID id, RoomDbo room, String url, String contentType, long size) {
        this.id = id;
        this.room = room;
        this.url = url;
        this.contentType = contentType;
        this.size = size;
    }

    public UUID getId() {
        return id;
    }

    public RoomDbo getRoom() {
        return room;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }
}
