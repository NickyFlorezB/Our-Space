package com.nicky.ourspace.application.usecases.authentication;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.port.AuthenticationPort;

@Service
public class LoginUserUseCase {

    private final AuthenticationPort authenticationPort;

    public LoginUserUseCase(AuthenticationPort authenticationPort) {
        this.authenticationPort = authenticationPort;
    }

    public String execute(String username, String password) {
        return authenticationPort.login(username, password);
    }

}
