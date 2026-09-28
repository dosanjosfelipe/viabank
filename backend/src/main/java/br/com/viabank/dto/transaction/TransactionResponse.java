package br.com.viabank.dto.transaction;
import java.math.BigDecimal;
import java.time.Instant;
public record TransactionResponse(
    Long id, String type, String name, String description, String category,
    BigDecimal amount, Instant createdAt, String transferReference
) {}
