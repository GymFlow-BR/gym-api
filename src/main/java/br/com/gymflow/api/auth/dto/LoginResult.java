package br.com.gymflow.api.auth.dto;

public record LoginResult(
        String token,
        AuthenticatedUserResponse user
) {
}