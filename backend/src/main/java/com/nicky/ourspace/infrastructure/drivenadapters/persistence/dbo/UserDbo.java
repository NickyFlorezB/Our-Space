package com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserDbo {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String cellphone;
    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "owner")
    private Set<RoomDbo> rooms = new HashSet<>();

    public UserDbo() {
    }

    public UserDbo(UUID id, String username, String email, String cellphone, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.cellphone = cellphone;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCellphone() {
        return cellphone;
    }

    public Set<RoomDbo> getRooms() {
        return rooms;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public void addRoom(RoomDbo room) {
        rooms.add(room);
        room.setOwner(this);
    }

    public void removeRoom(RoomDbo room) {
        if (rooms.remove(room) && room.getOwner() == this) {
            room.setOwner(null);
        }
    }

    void addRoomReference(RoomDbo room) {
        rooms.add(room);
    }

    void removeRoomReference(RoomDbo room) {
        rooms.remove(room);
    }

}
