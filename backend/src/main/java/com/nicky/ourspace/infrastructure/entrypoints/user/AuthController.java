package com.nicky.ourspace.infrastructure.entrypoints.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nicky.ourspace.application.dto.LoginResponseDto;
import com.nicky.ourspace.application.usecases.authentication.LoginUserUseCase;
import com.nicky.ourspace.application.usecases.user.FindUserByIdUseCase;
import com.nicky.ourspace.application.usecases.user.FindUserByUsernameUseCase;
import com.nicky.ourspace.infrastructure.drivenadapters.security.JwtService;
import com.nicky.ourspace.infrastructure.drivenadapters.security.RefreshTokenService;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUserUseCase loginUserUseCase;
    private final RefreshTokenService refreshTokenService;

    public AuthController(LoginUserUseCase loginUserUseCase, RefreshTokenService refreshTokenService,
            FindUserByIdUseCase findUserByIdUseCase,
            FindUserByUsernameUseCase findUserByUsernameUseCase, JwtService jwtService) {
        this.loginUserUseCase = loginUserUseCase;
        this.refreshTokenService = refreshTokenService;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of("userId", userId));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password must be provided");
        }

        return ResponseEntity.ok(loginUserUseCase.execute(username, password));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@RequestBody Map<String, String> entity) {
        String refreshToken = entity.get("refreshToken");

        return ResponseEntity.ok(refreshTokenService.refresh(refreshToken));
    }

}
