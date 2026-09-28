package br.com.viabank.service;

import br.com.viabank.repository.BankAccountRepository;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {
    private final SecureRandom random = new SecureRandom();
    private final BankAccountRepository accounts;
    public AccountNumberGenerator(BankAccountRepository accounts) { this.accounts = accounts; }

    public String next() {
        for (int i = 0; i < 30; i++) {
            String value = String.format("%08d", random.nextInt(100_000_000));
            if (!accounts.existsByAccountNumber(value)) return value;
        }
        throw new IllegalStateException("Não foi possível gerar um número de conta único.");
    }

    public String last4() { return String.format("%04d", random.nextInt(10_000)); }
}
