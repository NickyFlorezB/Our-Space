package com.nicky.ourspace.application.configuration.usecases;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nicky.ourspace.application.usecases.room.FindRoomByIdUseCase;
import com.nicky.ourspace.application.usecases.room.FindRoomsByOwnerIdUseCase;
import com.nicky.ourspace.application.usecases.room.SaveRoomUseCase;
import com.nicky.ourspace.domain.port.RoomMemberPort;
import com.nicky.ourspace.domain.port.RoomPort;

@Configuration
public class RoomUseCaseBeanConfiguration {

    @Bean
    FindRoomByIdUseCase findRoomByIdUseCase(RoomPort roomPort) {
        return new FindRoomByIdUseCase(roomPort);
    }

    @Bean
    FindRoomsByOwnerIdUseCase findRoomsByOwnerIdUseCase(RoomPort roomPort) {
        return new FindRoomsByOwnerIdUseCase(roomPort);
    }

    @Bean
    SaveRoomUseCase saveRoomUseCase(RoomPort roomPort, RoomMemberPort roomMemberPort) {
        return new SaveRoomUseCase(roomPort, roomMemberPort);
    }
}
