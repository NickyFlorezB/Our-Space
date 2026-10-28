package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.port.AuthenticationPort;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaUserRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.security.JwtService;

@Service
public class AuthenticationAdapter implements AuthenticationPort {

    private final JwtService jwtService;
    private final JpaUserRepository jpaUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationAdapter(JwtService jwtService, JpaUserRepository jpaUserRepository,
            PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.jpaUserRepository = jpaUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String login(String username, String password) {
        UserDbo userDbo = jpaUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, userDbo.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateAccessToken(userDbo.getId());
    }

}
