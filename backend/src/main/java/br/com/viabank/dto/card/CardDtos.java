package br.com.viabank.dto.card;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class CardDtos {
    private CardDtos() {}
    public record CardResponse(Long id, String type, String brand, String last4,
                               String holderName, String expiration, boolean blocked,
                               BigDecimal limit, BigDecimal usedLimit,
                               BigDecimal availableLimit, String productName) {}
    public record CardStatusRequest(boolean blocked) {}
    public record CardLimitRequest(@NotNull @DecimalMin("0.00") BigDecimal limit) {}
    public record VirtualCardResponse(Long id, String brand, String last4,
                                      String expiration, String message) {}
}
