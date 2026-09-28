package br.com.viabank.dto.analytics;
import java.math.BigDecimal;
import java.util.List;
public final class AnalyticsDtos {
    private AnalyticsDtos() {}
    public record SpendingResponse(BigDecimal total, List<CategorySpending> categories) {}
    public record CategorySpending(String name, BigDecimal amount) {}
    public record FinancialHealthResponse(Integer score, String label, String status, String description) {}
}
