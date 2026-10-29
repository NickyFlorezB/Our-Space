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
@Table(name = "movies")
public class MovieDbo {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomDbo room;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private String url;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    protected MovieDbo() {
    }

    public MovieDbo(UUID id, RoomDbo room, String name, String comment, String url, String imageUrl) {
        this.id = id;
        this.room = room;
        this.name = name;
        this.comment = comment;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public UUID getId() {
        return id;
    }

    public RoomDbo getRoom() {
        return room;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
