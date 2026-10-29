package com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class BookDbo {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomDbo room;

    @Column(nullable = false, unique = true)
    private String isbn_13;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String weight;

    @Column(name = "full_title")
    private String fullTitle;

    @Column(name = "latest_revision")
    private Integer latestRevision;

    @Column(name = "created_at")
    private String createdAt;

    private List<String> publishers;

    protected BookDbo() {
    }

    public BookDbo(UUID id, RoomDbo room, String isbn_13, String title, String weight, String fullTitle,
            Integer latestRevision, String createdAt, List<String> publishers) {
        this.id = id;
        this.room = room;
        this.isbn_13 = isbn_13;
        this.title = title;
        this.weight = weight;
        this.fullTitle = fullTitle;
        this.latestRevision = latestRevision;
        this.createdAt = createdAt;
        this.publishers = publishers;
    }

    public UUID getId() {
        return id;
    }

    public RoomDbo getRoom() {
        return room;
    }

    public String getIsbn_13() {
        return isbn_13;
    }

    public String getTitle() {
        return title;
    }

    public String getWeight() {
        return weight;
    }

    public String getFullTitle() {
        return fullTitle;
    }

    public Integer getLatestRevision() {
        return latestRevision;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<String> getPublishers() {
        return publishers;
    }

}
