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
@Table(name = "songs")
public class SongDbo {

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

    protected SongDbo() {
    }

    public SongDbo(UUID id, RoomDbo room, String name, String comment, String url) {
        this.id = id;
        this.room = room;
        this.name = name;
        this.comment = comment;
        this.url = url;
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

}
