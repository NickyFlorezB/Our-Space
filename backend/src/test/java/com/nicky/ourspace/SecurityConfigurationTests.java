package com.nicky.ourspace;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.application.usecases.user.SaveUserUseCase;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaUserRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.security.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;

@SpringBootTest(properties = {
        "security.jwt.secret=test-only-signing-key-with-at-least-32-bytes",
        "FRONTEND_ORIGINS=https://trusted.example"
})
@AutoConfigureMockMvc
@Transactional
class SecurityConfigurationTests {

    private static final String CONTEXT_PATH = "/api/v1";
    private static final String TRUSTED_ORIGIN = "https://trusted.example";
    private static final String TEST_SECRET = "test-only-signing-key-with-at-least-32-bytes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SaveUserUseCase saveUserUseCase;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Test
    void protectedEndpointRejectsRequestsWithoutJwtAndDoesNotCreateSession() throws Exception {
        mockMvc.perform(apiRequest(get(CONTEXT_PATH + "/auth/me")))
                .andExpect(status().isUnauthorized())
                .andDo(result -> assertNull(result.getRequest().getSession(false)));
    }

    @Test
    void validJwtAuthenticatesTheRequestWithoutCreatingSession() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(apiRequest(get(CONTEXT_PATH + "/auth/me"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateAccessToken(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andDo(result -> assertNull(result.getRequest().getSession(false)));
    }

    @Test
    void expiredAndInvalidlySignedJwtsAreRejected() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant expiration = Instant.now().minusSeconds(30);
        String expiredToken = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(expiration.minusSeconds(60)))
                .expiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        String invalidSignatureToken = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(
                        "another-test-signing-key-with-at-least-32-bytes".getBytes(StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(apiRequest(get(CONTEXT_PATH + "/auth/me"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(apiRequest(get(CONTEXT_PATH + "/auth/me"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidSignatureToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(apiRequest(post(CONTEXT_PATH + "/users"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidSignatureToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicRegistrationLoginAndRefreshKeepTheirContracts() throws Exception {
        String username = "security-" + UUID.randomUUID();
        String password = "test-password";
        String email = username + "@example.com";

        mockMvc.perform(apiRequest(post(CONTEXT_PATH + "/users"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "cellphone": "555-0100",
                                  "email": "%s"
                                }
                                """.formatted(username, password, email)))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(apiRequest(post(CONTEXT_PATH + "/auth/login"))
                        .header(HttpHeaders.ORIGIN, TRUSTED_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        Cookie refreshCookie = login.getResponse().getCookie("refreshToken");
        assertTrue(refreshCookie != null && refreshCookie.getValue() != null);

        MvcResult refresh = mockMvc.perform(apiRequest(post(CONTEXT_PATH + "/auth/refresh"))
                        .header(HttpHeaders.ORIGIN, TRUSTED_ORIGIN)
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn();
        Cookie rotatedCookie = refresh.getResponse().getCookie("refreshToken");
        assertTrue(rotatedCookie != null && rotatedCookie.getValue() != null);
        assertNotEquals(refreshCookie.getValue(), rotatedCookie.getValue());
        mockMvc.perform(apiRequest(post(CONTEXT_PATH + "/auth/refresh"))
                        .header(HttpHeaders.ORIGIN, TRUSTED_ORIGIN)
                        .cookie(refreshCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsAllowsOnlyTheConfiguredOrigin() throws Exception {
        mockMvc.perform(apiRequest(options(CONTEXT_PATH + "/auth/me"))
                        .header(HttpHeaders.ORIGIN, TRUSTED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, TRUSTED_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));

        mockMvc.perform(apiRequest(options(CONTEXT_PATH + "/auth/me"))
                        .header(HttpHeaders.ORIGIN, "https://untrusted.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void obsoleteMePathIsNotPublicAndAuthMeRemainsAuthenticated() throws Exception {
        mockMvc.perform(apiRequest(get(CONTEXT_PATH + "/me")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(apiRequest(get(CONTEXT_PATH + "/auth/me")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void securityContextDoesNotLeakIntoTheNextRequest() throws Exception {
        mockMvc.perform(apiRequest(get(CONTEXT_PATH + "/auth/me"))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwtService.generateAccessToken(UUID.randomUUID())))
                .andExpect(status().isOk());

        mockMvc.perform(apiRequest(get(CONTEXT_PATH + "/auth/me")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userResponsesDoNotExposePasswordAndUsersCannotModifyOtherAccounts() throws Exception {
        String ownerName = "owner-" + UUID.randomUUID();
        String targetName = "target-" + UUID.randomUUID();
        saveUserUseCase.execute(ownerName, "owner-password", "555-0101", ownerName + "@example.com");
        saveUserUseCase.execute(targetName, "target-password", "555-0102", targetName + "@example.com");
        UUID ownerId = userRepository.findByUsername(ownerName).orElseThrow().getId();
        UUID targetId = userRepository.findByUsername(targetName).orElseThrow().getId();
        String ownerToken = jwtService.generateAccessToken(ownerId);

        mockMvc.perform(apiRequest(get(CONTEXT_PATH + "/users/" + targetId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(apiRequest(put(CONTEXT_PATH + "/users/" + targetId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "hijacked",
                                  "password": "new-password",
                                  "cellphone": "555-0199",
                                  "email": "hijacked@example.com"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(apiRequest(delete(CONTEXT_PATH + "/users/" + targetId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isForbidden());

        assertTrue(userRepository.existsById(targetId));
        assertTrue(applicationContext.getBeansOfType(UserDetailsService.class).isEmpty());
        assertFalse(environment.getProperty("spring.jpa.open-in-view", Boolean.class, true));
    }

    private MockHttpServletRequestBuilder apiRequest(MockHttpServletRequestBuilder request) {
        return request.contextPath(CONTEXT_PATH);
    }
}
