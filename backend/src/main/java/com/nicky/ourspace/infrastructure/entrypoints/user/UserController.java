package com.nicky.ourspace.infrastructure.entrypoints.user;

import com.nicky.ourspace.application.dto.UserResponseDto;
import com.nicky.ourspace.application.usecases.user.DeleteUserUseCase;
import com.nicky.ourspace.application.usecases.user.FindAllUsersUseCase;
import com.nicky.ourspace.application.usecases.user.FindUserByEmailUseCase;
import com.nicky.ourspace.application.usecases.user.FindUserByIdUseCase;
import com.nicky.ourspace.application.usecases.user.FindUserByUsernameUseCase;
import com.nicky.ourspace.application.usecases.user.SaveUserUseCase;
import com.nicky.ourspace.application.usecases.user.UpdateUserUseCase;
import com.nicky.ourspace.domain.model.User;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final FindAllUsersUseCase findAllUsersUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final FindUserByEmailUseCase findUserByEmailUseCase;
    private final FindUserByUsernameUseCase findUserByUsernameUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final SaveUserUseCase saveUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    public UserController(
            FindAllUsersUseCase findAllUsersUseCase,
            FindUserByIdUseCase findUserByIdUseCase,
            FindUserByEmailUseCase findUserByEmailUseCase,
            FindUserByUsernameUseCase findUserByUsernameUseCase,
            DeleteUserUseCase deleteUserUseCase,
            SaveUserUseCase saveUserUseCase,
            UpdateUserUseCase updateUserUseCase) {
        this.findAllUsersUseCase = findAllUsersUseCase;
        this.findUserByIdUseCase = findUserByIdUseCase;
        this.findUserByEmailUseCase = findUserByEmailUseCase;
        this.findUserByUsernameUseCase = findUserByUsernameUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.saveUserUseCase = saveUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<UserResponseDto>> findAll() {
        return ResponseEntity.ok().body(findAllUsersUseCase.execute().stream()
                .map(UserResponseDto::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findById(@PathVariable("id") String id) {
        return ResponseEntity.ok().body(UserResponseDto.from(findUserByIdUseCase.execute(UUID.fromString(id))));
    }

    @GetMapping(path = "/find", params = "email")
    public ResponseEntity<UserResponseDto> findByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok().body(UserResponseDto.from(findUserByEmailUseCase.execute(email)));
    }

    @GetMapping(path = "/find", params = "username")
    public ResponseEntity<UserResponseDto> findByUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok().body(UserResponseDto.from(findUserByUsernameUseCase.execute(username)));
    }

    @PostMapping("")
    public ResponseEntity<Void> save(@RequestBody User entity) {
        saveUserUseCase.execute(
                entity.username(),
                entity.password(),
                entity.cellphone(),
                entity.email());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable("id") String id,
            @RequestBody User entity,
            Authentication authentication) {
        UUID userId = requireOwnUser(id, authentication);
        return ResponseEntity.ok().body(UserResponseDto.from(updateUserUseCase.execute(entity, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id, Authentication authentication) {
        deleteUserUseCase.execute(requireOwnUser(id, authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID requireOwnUser(String id, Authentication authentication) {
        UUID requestedUserId = UUID.fromString(id);
        UUID authenticatedUserId = (UUID) authentication.getPrincipal();
        if (!requestedUserId.equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Users can only modify their own account");
        }
        return requestedUserId;
    }

}
