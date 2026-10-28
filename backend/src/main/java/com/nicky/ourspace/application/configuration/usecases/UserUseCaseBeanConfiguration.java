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
import com.nicky.ourspace.infrastructure.drivenadapters.adapters.UserAdapter;

@Configuration
public class UserUseCaseBeanConfiguration {

    @Bean
    FindAllUsersUseCase findAllUsersUseCase(
            UserAdapter userPort) {
        return new FindAllUsersUseCase(userPort);
    }

    @Bean
    FindUserByIdUseCase findUserByIdUseCase(
            UserAdapter userPort) {
        return new FindUserByIdUseCase(userPort);
    }

    @Bean
    FindUserByEmailUseCase findUserByEmailUseCase(
            UserAdapter userPort) {
        return new FindUserByEmailUseCase(userPort);
    }

    @Bean
    FindUserByUsernameUseCase findUserByUsernameUseCase(
            UserAdapter userPort) {
        return new FindUserByUsernameUseCase(userPort);
    }

    @Bean
    DeleteUserUseCase deleteUserUseCase(
            UserAdapter userPort) {
        return new DeleteUserUseCase(userPort);
    }

    @Bean
    SaveUserUseCase saveUserUseCase(
            UserAdapter userPort) {
        return new SaveUserUseCase(userPort);
    }

    @Bean
    UpdateUserUseCase updateUserUseCase(
            UserAdapter userPort) {
        return new UpdateUserUseCase(userPort);
    }

}
