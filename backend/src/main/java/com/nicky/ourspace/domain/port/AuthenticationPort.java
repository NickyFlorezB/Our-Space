package com.nicky.ourspace.domain.port;

public interface AuthenticationPort {

    String login(String username, String password);
}
