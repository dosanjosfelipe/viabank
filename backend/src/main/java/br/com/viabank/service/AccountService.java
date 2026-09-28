package br.com.viabank.service;

import br.com.viabank.domain.TransactionType;
import br.com.viabank.dto.account.AccountDtos.*;
import br.com.viabank.entity.*;
import br.com.viabank.exception.*;
import br.com.viabank.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.*;

@Service
public class AccountService {
    private final CurrentUserService currentUser;
    private final BankAccountRepository accounts;
    private final BankTransactionRepository transactions;
    private final boolean demoDeposit;

    public AccountService(CurrentUserService currentUser, BankAccountRepository accounts,
                          BankTransactionRepository transactions,
                          @Value("${app.features.demo-deposit:false}") boolean demoDeposit) {
        this.currentUser = currentUser; this.accounts = accounts; this.transactions = transactions;
        this.demoDeposit = demoDeposit;
    }

    public AccountResponse get() {
        BankAccount a = account();
        return new AccountResponse(a.getId(), a.getAgency(), maskAccount(a.getAccountNumber()),
            a.getBalance(), a.getStatus().name().toLowerCase(), a.getYieldLabel());
    }

    public BalanceResponse balance() { return new BalanceResponse(account().getBalance()); }

    @Transactional
    public DepositResponse deposit(DepositRequest request) {
        if (!demoDeposit) throw new ForbiddenException("Depósito manual está desabilitado.");
        BankAccount base = account();
        BankAccount account = accounts.findByIdForUpdate(base.getId())
            .orElseThrow(() -> new NotFoundException("Conta não encontrada."));
        BigDecimal amount = money(request.amount());
        account.setBalance(account.getBalance().add(amount));

        BankTransaction tx = new BankTransaction();
        tx.setAccount(account); tx.setType(TransactionType.DEPOSIT); tx.setAmount(amount);
        tx.setName("Depósito");
        tx.setDescription(request.description() == null || request.description().isBlank()
            ? "Crédito de desenvolvimento" : request.description().trim());
        tx.setCategory("Depósitos");
        transactions.save(tx);
        return new DepositResponse(account.getBalance(), "Depósito realizado.");
    }

    public BankAccount account() {
        User user = currentUser.get();
        return accounts.findByUserId(user.getId())
            .orElseThrow(() -> new NotFoundException("Conta bancária não encontrada."));
    }

    public static BigDecimal money(BigDecimal value) {
        if (value == null) throw new BusinessException("Valor obrigatório.");
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    private String maskAccount(String number) {
        if (number == null || number.length() < 4) return "••••";
        return "•••• " + number.substring(number.length() - 4);
    }
}
