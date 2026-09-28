package br.com.viabank.controller;
import br.com.viabank.dto.account.AccountDtos.*;
import br.com.viabank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService service;
    public AccountController(AccountService service) { this.service = service; }
    @GetMapping AccountResponse get() { return service.get(); }
    @GetMapping("/balance") BalanceResponse balance() { return service.balance(); }
    @PostMapping("/deposit") DepositResponse deposit(@Valid @RequestBody DepositRequest request) { return service.deposit(request); }
}
