package com.nicky.ourspace.infrastructure.entrypoints.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nicky.ourspace.application.dto.AuthenticationTokensDto;
import com.nicky.ourspace.application.dto.LoginResponseDto;
import com.nicky.ourspace.application.usecases.authentication.LoginUserUseCase;
import com.nicky.ourspace.infrastructure.drivenadapters.security.RefreshTokenService;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUserUseCase loginUserUseCase;
    private final RefreshTokenService refreshTokenService;
    private final String refreshCookieName;
    private final boolean refreshCookieSecure;
    private final String refreshCookieSameSite;
    private final String refreshCookiePath;
    private final List<String> allowedOrigins;

    public AuthController(
            LoginUserUseCase loginUserUseCase,
            RefreshTokenService refreshTokenService,
            @Value("${app.auth.refresh-cookie.name:refreshToken}") String refreshCookieName,
            @Value("${app.auth.refresh-cookie.secure:false}") boolean refreshCookieSecure,
            @Value("${app.auth.refresh-cookie.same-site:Lax}") String refreshCookieSameSite,
            @Value("${app.auth.refresh-cookie.path:${server.servlet.context-path:/api/v1}/auth}") String refreshCookiePath,
            @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}") List<String> allowedOrigins) {
        this.loginUserUseCase = loginUserUseCase;
        this.refreshTokenService = refreshTokenService;
        this.refreshCookieName = refreshCookieName;
        this.refreshCookieSecure = refreshCookieSecure;
        this.refreshCookieSameSite = refreshCookieSameSite;
        this.refreshCookiePath = refreshCookiePath;
        this.allowedOrigins = allowedOrigins.stream().map(String::trim).toList();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of("userId", userId));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            HttpServletRequest request,
            @RequestBody Map<String, String> credentials) {
        validateOrigin(request);
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password must be provided");
        }

        AuthenticationTokensDto tokens = loginUserUseCase.execute(username, password);
        return tokenResponse(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
            HttpServletRequest request,
            @CookieValue(name = "${app.auth.refresh-cookie.name:refreshToken}", required = false) String refreshToken) {
        validateOrigin(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token cookie is missing");
        }

        return tokenResponse(refreshTokenService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            @CookieValue(name = "${app.auth.refresh-cookie.name:refreshToken}", required = false) String refreshToken) {
        validateOrigin(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoke(refreshToken);
        }

        ResponseCookie expiredCookie = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path(refreshCookiePath)
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }

    private ResponseEntity<LoginResponseDto> tokenResponse(AuthenticationTokensDto tokens) {
        ResponseCookie refreshCookie = ResponseCookie.from(refreshCookieName, tokens.refreshToken())
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path(refreshCookiePath)
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new LoginResponseDto(tokens.accessToken()));
    }

    private void validateOrigin(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (!allowedOrigins.contains(origin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request origin is not allowed");
        }
    }

}
