package com.nicky.ourspace.infrastructure.entrypoints.room;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nicky.ourspace.application.dto.AddRoomMemberRequest;
import com.nicky.ourspace.application.usecases.roommember.AddRoomMemberUseCase;
import com.nicky.ourspace.application.usecases.roommember.FindRoomMembersByRoomIdUseCase;
import com.nicky.ourspace.application.usecases.roommember.FindRoomMembersByUserIdUseCase;
import com.nicky.ourspace.application.usecases.roommember.RemoveRoomMemberUseCase;
import com.nicky.ourspace.domain.model.RoomMember;

@RestController
public class RoomMemberController {

    private final AddRoomMemberUseCase addRoomMemberUseCase;
    private final FindRoomMembersByRoomIdUseCase findRoomMembersByRoomIdUseCase;
    private final FindRoomMembersByUserIdUseCase findRoomMembersByUserIdUseCase;
    private final RemoveRoomMemberUseCase removeRoomMemberUseCase;

    public RoomMemberController(
            AddRoomMemberUseCase addRoomMemberUseCase,
            FindRoomMembersByRoomIdUseCase findRoomMembersByRoomIdUseCase,
            FindRoomMembersByUserIdUseCase findRoomMembersByUserIdUseCase,
            RemoveRoomMemberUseCase removeRoomMemberUseCase) {
        this.addRoomMemberUseCase = addRoomMemberUseCase;
        this.findRoomMembersByRoomIdUseCase = findRoomMembersByRoomIdUseCase;
        this.findRoomMembersByUserIdUseCase = findRoomMembersByUserIdUseCase;
        this.removeRoomMemberUseCase = removeRoomMemberUseCase;
    }

    @PostMapping("/room/{roomId}/members")
    public ResponseEntity<RoomMember> addMember(
            @PathVariable UUID roomId,
            @Valid @RequestBody AddRoomMemberRequest request,
            Authentication authentication) {
        RoomMember roomMember = addRoomMemberUseCase.execute(
                roomId,
                authenticatedUserId(authentication),
                request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(roomMember);
    }

    @GetMapping("/room/{roomId}/members")
    public ResponseEntity<List<RoomMember>> findMembers(
            @PathVariable UUID roomId,
            Authentication authentication) {
        return ResponseEntity.ok(findRoomMembersByRoomIdUseCase.execute(
                roomId,
                authenticatedUserId(authentication)));
    }

    @GetMapping("/room-members/user/{userId}")
    public ResponseEntity<List<RoomMember>> findUserMemberships(
            @PathVariable UUID userId,
            Authentication authentication) {
        return ResponseEntity.ok(findRoomMembersByUserIdUseCase.execute(
                userId,
                authenticatedUserId(authentication)));
    }

    @DeleteMapping("/room/{roomId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID roomId,
            @PathVariable UUID userId,
            Authentication authentication) {
        removeRoomMemberUseCase.execute(
                roomId,
                authenticatedUserId(authentication),
                userId);
        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}
