package com.nicky.ourspace.domain.model;

import java.util.UUID;

public record User(
                UUID id,
                String username,
                String password,
                String cellphone,
                String email) {

        public User(UUID id, String username, String password, String cellphone, String email) {
                this.id = id;
                this.username = username;
                this.password = password;
                this.cellphone = cellphone;
                this.email = email;
        }
}
