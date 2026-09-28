package br.com.viabank.service;

import br.com.viabank.dto.transaction.TransactionResponse;
import br.com.viabank.entity.BankTransaction;
import br.com.viabank.exception.NotFoundException;
import br.com.viabank.repository.BankTransactionRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Locale;

@Service
public class TransactionService {
    private final AccountService accountService;
    private final BankTransactionRepository transactions;

    public TransactionService(AccountService accountService, BankTransactionRepository transactions) {
        this.accountService = accountService; this.transactions = transactions;
    }

    public Page<TransactionResponse> list(String period, int page, int limit) {
        var account = accountService.account();
        Range range = parsePeriod(period);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(limit, 1), 200));
        return transactions.findStatement(account.getId(), range.from(), range.to(), pageable).map(this::map);
    }

    public TransactionResponse get(Long id) {
        var account = accountService.account();
        BankTransaction tx = transactions.findById(id)
            .filter(t -> t.getAccount().getId().equals(account.getId()))
            .orElseThrow(() -> new NotFoundException("Transação não encontrada."));
        return map(tx);
    }

    private TransactionResponse map(BankTransaction tx) {
        return new TransactionResponse(tx.getId(), tx.getType().name().toLowerCase(Locale.ROOT),
            tx.getName(), tx.getDescription(), tx.getCategory(), tx.getAmount(),
            tx.getCreatedAt(), tx.getTransferReference());
    }

    public Range parsePeriod(String period) {
        Instant now = Instant.now();
        if (period == null || period.isBlank() || period.equalsIgnoreCase("30d"))
            return new Range(now.minus(Duration.ofDays(30)), now.plusSeconds(1));
        if (period.equalsIgnoreCase("90d"))
            return new Range(now.minus(Duration.ofDays(90)), now.plusSeconds(1));
        if (period.matches("\\d{4}")) {
            int year = Integer.parseInt(period);
            ZoneId zone = ZoneId.of("America/Sao_Paulo");
            return new Range(
                LocalDate.of(year,1,1).atStartOfDay(zone).toInstant(),
                LocalDate.of(year+1,1,1).atStartOfDay(zone).toInstant()
            );
        }
        throw new IllegalArgumentException("Período inválido. Use 30d, 90d ou um ano, como 2026.");
    }

    public record Range(Instant from, Instant to) {}
}
