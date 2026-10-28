package com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

}
