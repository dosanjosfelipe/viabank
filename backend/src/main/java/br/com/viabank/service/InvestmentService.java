package br.com.viabank.service;

import br.com.viabank.domain.TransactionType;
import br.com.viabank.dto.investment.InvestmentDtos.*;
import br.com.viabank.entity.*;
import br.com.viabank.exception.*;
import br.com.viabank.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.Locale;

@Service
public class InvestmentService {
    private final AccountService accountService;
    private final BankAccountRepository accounts;
    private final InvestmentProductRepository products;
    private final InvestmentRepository investments;
    private final BankTransactionRepository transactions;

    public InvestmentService(AccountService accountService, BankAccountRepository accounts,
                             InvestmentProductRepository products, InvestmentRepository investments,
                             BankTransactionRepository transactions) {
        this.accountService = accountService; this.accounts = accounts; this.products = products;
        this.investments = investments; this.transactions = transactions;
    }

    public List<ProductResponse> products() {
        return products.findAllByActiveTrueOrderByIdAsc().stream()
            .map(p -> new ProductResponse(p.getId(), p.getName(), p.getDescription(),
                p.getRisk().name().toLowerCase(Locale.ROOT), riskLabel(p), p.getReturnLabel()))
            .toList();
    }

    public PortfolioResponse portfolio() {
        var list = investments.findAllByAccountIdOrderByIdAsc(accountService.account().getId());
        BigDecimal total = list.stream().map(Investment::getCurrentValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthly = list.stream().map(Investment::getMonthlyReturn).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PortfolioResponse(total, monthly);
    }

    @Transactional
    public InvestmentResponse invest(InvestRequest request) {
        BankAccount base = accountService.account();
        BankAccount account = accounts.findByIdForUpdate(base.getId())
            .orElseThrow(() -> new NotFoundException("Conta não encontrada."));
        InvestmentProduct product = products.findById(request.productId())
            .filter(InvestmentProduct::isActive)
            .orElseThrow(() -> new NotFoundException("Produto de investimento não encontrado."));
        BigDecimal amount = AccountService.money(request.amount());
        if (account.getBalance().compareTo(amount) < 0) throw new BusinessException("Saldo insuficiente.");

        account.setBalance(account.getBalance().subtract(amount));
        Investment inv = new Investment();
        inv.setAccount(account); inv.setProduct(product); inv.setPrincipal(amount);
        inv.setCurrentValue(amount); inv.setMonthlyReturn(BigDecimal.ZERO);
        investments.save(inv);

        BankTransaction tx = new BankTransaction();
        tx.setAccount(account); tx.setType(TransactionType.INVESTMENT); tx.setAmount(amount.negate());
        tx.setName("Investimento"); tx.setDescription(product.getName()); tx.setCategory("Investimentos");
        transactions.save(tx);
        return map(inv);
    }

    @Transactional
    public InvestmentResponse redeem(Long id, RedeemRequest request) {
        BankAccount base = accountService.account();
        BankAccount account = accounts.findByIdForUpdate(base.getId())
            .orElseThrow(() -> new NotFoundException("Conta não encontrada."));
        Investment inv = investments.findByIdAndAccountId(id, account.getId())
            .orElseThrow(() -> new NotFoundException("Investimento não encontrado."));
        BigDecimal amount = AccountService.money(request.amount());
        if (inv.getCurrentValue().compareTo(amount) < 0)
            throw new BusinessException("Valor de resgate maior que o saldo do investimento.");

        inv.setCurrentValue(inv.getCurrentValue().subtract(amount));
        inv.setPrincipal(inv.getPrincipal().subtract(amount).max(BigDecimal.ZERO));
        account.setBalance(account.getBalance().add(amount));

        BankTransaction tx = new BankTransaction();
        tx.setAccount(account); tx.setType(TransactionType.REDEMPTION); tx.setAmount(amount);
        tx.setName("Resgate de investimento"); tx.setDescription(inv.getProduct().getName());
        tx.setCategory("Investimentos"); transactions.save(tx);

        InvestmentResponse response = map(inv);
        if (inv.getCurrentValue().compareTo(BigDecimal.ZERO) == 0) investments.delete(inv);
        else investments.save(inv);
        return response;
    }

    private InvestmentResponse map(Investment i) {
        return new InvestmentResponse(i.getId(), i.getProduct().getId(), i.getProduct().getName(),
            i.getPrincipal(), i.getCurrentValue());
    }

    private String riskLabel(InvestmentProduct p) {
        return switch (p.getRisk()) {
            case LOW -> "Baixo risco";
            case MODERATE -> "Risco moderado";
            case HIGH -> "Alto risco";
        };
    }
}
