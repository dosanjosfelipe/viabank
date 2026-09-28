package br.com.viabank.dto.pix;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public final class PixDtos {
    private PixDtos() {}
    public record CreateKeyRequest(@NotBlank String type, String value) {}
    public record PixKeyResponse(Long id, String type, String value, String maskedValue) {}
    public record SendPixRequest(@NotBlank String key,
                                 @NotNull @DecimalMin("0.01") BigDecimal amount,
                                 @Size(max = 255) String description) {}
    public record PixTransferResponse(String transferId, BigDecimal amount,
                                      String receiverName, Instant createdAt, String status) {}
    public record AddFavoriteRequest(@NotBlank @Size(max = 120) String name,
                                     @NotBlank @Size(max = 180) String pixKey,
                                     @Size(max = 120) String bankName) {}
    public record FavoriteResponse(Long id, String name, String pixKey,
                                   String maskedKey, String bankName) {}
}
