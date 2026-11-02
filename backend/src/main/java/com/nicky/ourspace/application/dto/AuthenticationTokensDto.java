package com.nicky.ourspace.application.dto;

public record AuthenticationTokensDto(
        String accessToken,
        String refreshToken) {

}
