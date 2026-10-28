package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.port.AuthenticationPort;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaUserRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.security.JwtService;
import com.nicky.ourspace.infrastructure.drivenadapters.security.RefreshTokenService;

@Service
public class AuthenticationAdapter implements AuthenticationPort {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JpaUserRepository jpaUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationAdapter(JwtService jwtService, RefreshTokenService refreshTokenService,
            JpaUserRepository jpaUserRepository,
            PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.jpaUserRepository = jpaUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Map<String, String> login(String username, String password) {
        UserDbo userDbo = jpaUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, userDbo.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return new HashMap<>() {
            {
                put("accessToken", jwtService.generateAccessToken(userDbo.getId()));
                put("refreshToken", refreshTokenService.create(userDbo.getId()));
            }
        };
    }

}
