package com.nicky.ourspace.application.configuration.usecases;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nicky.ourspace.application.usecases.room.FindRoomByIdUseCase;
import com.nicky.ourspace.application.usecases.room.FindRoomsByOwnerIdUseCase;
import com.nicky.ourspace.application.usecases.room.SaveRoomUseCase;
import com.nicky.ourspace.infrastructure.drivenadapters.adapters.RoomAdapter;

@Configuration
public class RoomUseCaseBeanConfiguration {

    @Bean
    FindRoomByIdUseCase findRoomByIdUseCase(RoomAdapter roomAdapter) {
        return new FindRoomByIdUseCase(roomAdapter);
    }

    @Bean
    FindRoomsByOwnerIdUseCase findRoomsByOwnerIdUseCase(RoomAdapter roomAdapter) {
        return new FindRoomsByOwnerIdUseCase(roomAdapter);
    }

    @Bean
    SaveRoomUseCase saveRoomUseCase(RoomAdapter roomAdapter) {
        return new SaveRoomUseCase(roomAdapter);
    }
}
