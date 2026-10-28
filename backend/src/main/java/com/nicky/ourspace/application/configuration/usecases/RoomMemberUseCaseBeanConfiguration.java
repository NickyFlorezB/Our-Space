package com.nicky.ourspace.application.configuration.usecases;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nicky.ourspace.application.usecases.roommember.AddRoomMemberUseCase;
import com.nicky.ourspace.application.usecases.roommember.FindRoomMembersByRoomIdUseCase;
import com.nicky.ourspace.application.usecases.roommember.FindRoomMembersByUserIdUseCase;
import com.nicky.ourspace.application.usecases.roommember.RemoveRoomMemberUseCase;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Configuration
public class RoomMemberUseCaseBeanConfiguration {

    @Bean
    AddRoomMemberUseCase addRoomMemberUseCase(RoomMemberPort roomMemberPort) {
        return new AddRoomMemberUseCase(roomMemberPort);
    }

    @Bean
    FindRoomMembersByRoomIdUseCase findRoomMembersByRoomIdUseCase(RoomMemberPort roomMemberPort) {
        return new FindRoomMembersByRoomIdUseCase(roomMemberPort);
    }

    @Bean
    FindRoomMembersByUserIdUseCase findRoomMembersByUserIdUseCase(RoomMemberPort roomMemberPort) {
        return new FindRoomMembersByUserIdUseCase(roomMemberPort);
    }

    @Bean
    RemoveRoomMemberUseCase removeRoomMemberUseCase(RoomMemberPort roomMemberPort) {
        return new RemoveRoomMemberUseCase(roomMemberPort);
    }
}
