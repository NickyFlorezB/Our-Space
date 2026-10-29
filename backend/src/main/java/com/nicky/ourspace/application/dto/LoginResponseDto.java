package com.nicky.ourspace.application.dto;

public record LoginResponseDto(
        String accessToken,
        String refreshToken) {

}
