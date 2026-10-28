package com.nicky.ourspace.application.configuration.usecases;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nicky.ourspace.application.usecases.authentication.LoginUserUseCase;
import com.nicky.ourspace.infrastructure.drivenadapters.adapters.AuthenticationAdapter;

@Configuration
public class AuthenticationUseCaseBeanConfiguration {

    @Bean
    public LoginUserUseCase loginUserUseCase(AuthenticationAdapter authenticationPort) {
        return new LoginUserUseCase(authenticationPort);
    }

}
