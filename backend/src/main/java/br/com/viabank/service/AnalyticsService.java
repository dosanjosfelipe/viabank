package br.com.viabank.service;

import br.com.viabank.dto.analytics.AnalyticsDtos.*;
import br.com.viabank.repository.BankTransactionRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AnalyticsService {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final BankTransactionRepository transactions;

    public AnalyticsService(AccountService accountService, TransactionService transactionService,
                            BankTransactionRepository transactions) {
        this.accountService = accountService; this.transactionService = transactionService; this.transactions = transactions;
    }

    public SpendingResponse spending(String period) {
        var account = accountService.account();
        var range = transactionService.parsePeriod(period);
        List<CategorySpending> categories = transactions
            .sumExpensesByCategory(account.getId(), range.from(), range.to()).stream()
            .map(p -> new CategorySpending(p.getCategory(), p.getTotal()))
            .toList();
        BigDecimal total = categories.stream().map(CategorySpending::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SpendingResponse(total, categories);
    }

    public FinancialHealthResponse health() {
        var account = accountService.account();
        int score = 500;
        if (account.getBalance().signum() > 0) score += 100;
        if (account.getBalance().compareTo(new BigDecimal("1000.00")) >= 0) score += 100;
        score = Math.min(score, 1000);
        String label = score >= 700 ? "Saudável" : score >= 550 ? "Em evolução" : "Inicial";
        return new FinancialHealthResponse(score, label, "available",
            "Indicador interno do Via Bank calculado a partir dos dados da conta; não é score de bureau de crédito.");
    }
}
