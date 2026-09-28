package br.com.viabank.dto.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public final class AccountDtos {
    private AccountDtos() {}
    public record AccountResponse(Long id, String agency, String accountNumberMasked,
                                  BigDecimal balance, String status, String yieldLabel) {}
    public record BalanceResponse(BigDecimal balance) {}
    public record DepositRequest(@NotNull @DecimalMin("0.01") BigDecimal amount,
                                 @Size(max = 255) String description) {}
    public record DepositResponse(BigDecimal balance, String message) {}
}
