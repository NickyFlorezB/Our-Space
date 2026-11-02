package com.nicky.ourspace.application.usecases.authentication;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.application.dto.AuthenticationTokensDto;
import com.nicky.ourspace.domain.port.AuthenticationPort;

@Service
public class LoginUserUseCase {

    private final AuthenticationPort authenticationPort;

    public LoginUserUseCase(AuthenticationPort authenticationPort) {
        this.authenticationPort = authenticationPort;
    }

    public AuthenticationTokensDto execute(String username, String password) {
        Map<String, String> tokens = authenticationPort.login(username, password);
        return new AuthenticationTokensDto(tokens.get("accessToken"), tokens.get("refreshToken"));
    }

}
