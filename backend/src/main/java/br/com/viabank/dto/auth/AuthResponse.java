package br.com.viabank.dto.auth;
public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    UserSummary user,
    String message
) {
    public record UserSummary(Long id, String name, String email) {}
}
