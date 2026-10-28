package com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo;

import com.nicky.ourspace.domain.model.RoomMemberRole;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "room_members")
public class RoomMemberDbo {

    @EmbeddedId
    private RoomMemberId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("roomId")
    @JoinColumn(name = "room_id", nullable = false)
    private RoomDbo room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserDbo user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RoomMemberRole role;

    protected RoomMemberDbo() {
    }

    public RoomMemberDbo(RoomMemberId id, RoomDbo room, UserDbo user, RoomMemberRole role) {
        this.id = id;
        this.room = room;
        this.user = user;
        this.role = role;
    }

    public RoomMemberId getId() {
        return id;
    }

    public RoomDbo getRoom() {
        return room;
    }

    public UserDbo getUser() {
        return user;
    }

    public RoomMemberRole getRole() {
        return role;
    }
}
