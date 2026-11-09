package com.nicky.ourspace.infrastructure.entrypoints.notifications;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nicky.ourspace.application.dto.PushSettingsResponse;
import com.nicky.ourspace.application.dto.PushSubscriptionDeleteRequest;
import com.nicky.ourspace.application.dto.PushSubscriptionRequest;
import com.nicky.ourspace.application.usecases.notifications.ManagePushSubscriptionUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/push")
public class PushSubscriptionController {

    private final ManagePushSubscriptionUseCase managePushSubscriptionUseCase;

    public PushSubscriptionController(ManagePushSubscriptionUseCase managePushSubscriptionUseCase) {
        this.managePushSubscriptionUseCase = managePushSubscriptionUseCase;
    }

    @GetMapping("/public-key")
    public ResponseEntity<PushSettingsResponse> settings() {
        return ResponseEntity.ok(managePushSubscriptionUseCase.settings());
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<Void> subscribe(
            @Valid @RequestBody PushSubscriptionRequest request,
            Authentication authentication) {
        managePushSubscriptionUseCase.subscribe(
                authenticatedUserId(authentication),
                request.endpoint(),
                request.keys().p256dh(),
                request.keys().auth());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/subscriptions")
    public ResponseEntity<Void> unsubscribe(
            @Valid @RequestBody PushSubscriptionDeleteRequest request,
            Authentication authentication) {
        managePushSubscriptionUseCase.unsubscribe(authenticatedUserId(authentication), request.endpoint());
        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}
