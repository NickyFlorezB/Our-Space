package com.nicky.ourspace.application.configuration.usecases;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nicky.ourspace.application.usecases.user.DeleteUserUseCase;
import com.nicky.ourspace.application.usecases.user.FindAllUsersUseCase;
import com.nicky.ourspace.application.usecases.user.FindUserByEmailUseCase;
import com.nicky.ourspace.application.usecases.user.FindUserByIdUseCase;
import com.nicky.ourspace.application.usecases.user.FindUserByUsernameUseCase;
import com.nicky.ourspace.application.usecases.user.SaveUserUseCase;
import com.nicky.ourspace.application.usecases.user.UpdateUserUseCase;
import com.nicky.ourspace.domain.port.UserPort;

@Configuration
public class UserUseCaseBeanConfiguration {

    @Bean
    FindAllUsersUseCase findAllUsersUseCase(
            UserPort userPort) {
        return new FindAllUsersUseCase(userPort);
    }

    @Bean
    FindUserByIdUseCase findUserByIdUseCase(
            UserPort userPort) {
        return new FindUserByIdUseCase(userPort);
    }

    @Bean
    FindUserByEmailUseCase findUserByEmailUseCase(
            UserPort userPort) {
        return new FindUserByEmailUseCase(userPort);
    }

    @Bean
    FindUserByUsernameUseCase findUserByUsernameUseCase(
            UserPort userPort) {
        return new FindUserByUsernameUseCase(userPort);
    }

    @Bean
    DeleteUserUseCase deleteUserUseCase(
            UserPort userPort) {
        return new DeleteUserUseCase(userPort);
    }

    @Bean
    SaveUserUseCase saveUserUseCase(
            UserPort userPort) {
        return new SaveUserUseCase(userPort);
    }

    @Bean
    UpdateUserUseCase updateUserUseCase(
            UserPort userPort) {
        return new UpdateUserUseCase(userPort);
    }

}
