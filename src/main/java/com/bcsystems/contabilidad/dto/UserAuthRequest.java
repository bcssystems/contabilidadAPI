package com.bcsystems.contabilidad.dto;

public record UserAuthRequest(
        String email,
        String password
) {
}
