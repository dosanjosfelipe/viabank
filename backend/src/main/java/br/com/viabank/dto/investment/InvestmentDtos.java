package br.com.viabank.dto.investment;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class InvestmentDtos {
    private InvestmentDtos() {}
    public record ProductResponse(Long id, String name, String description,
                                  String risk, String riskLabel, String returnLabel) {}
    public record PortfolioResponse(BigDecimal total, BigDecimal monthlyReturn) {}
    public record InvestRequest(@NotNull Long productId,
                                @NotNull @DecimalMin("0.01") BigDecimal amount) {}
    public record RedeemRequest(@NotNull @DecimalMin("0.01") BigDecimal amount) {}
    public record InvestmentResponse(Long id, Long productId, String productName,
                                     BigDecimal principal, BigDecimal currentValue) {}
}
