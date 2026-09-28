package br.com.viabank.service;

import br.com.viabank.domain.*;
import br.com.viabank.dto.auth.*;
import br.com.viabank.entity.*;
import br.com.viabank.exception.ConflictException;
import br.com.viabank.repository.*;
import br.com.viabank.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository users;
    private final BankAccountRepository accounts;
    private final PixKeyRepository pixKeys;
    private final UserSettingsRepository settings;
    private final CardRepository cards;
    private final RevokedTokenRepository revokedTokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountNumberGenerator generator;

    public AuthService(UserRepository users, BankAccountRepository accounts, PixKeyRepository pixKeys,
                       UserSettingsRepository settings, CardRepository cards,
                       RevokedTokenRepository revokedTokens, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       AccountNumberGenerator generator) {
        this.users = users; this.accounts = accounts; this.pixKeys = pixKeys; this.settings = settings;
        this.cards = cards; this.revokedTokens = revokedTokens; this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager; this.jwtService = jwtService; this.generator = generator;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String cpf = request.cpf().replaceAll("\\D", "");
        String phone = request.phone().replaceAll("\\D", "");

        if (users.existsByEmailIgnoreCase(email)) throw new ConflictException("E-mail já cadastrado.");
        if (users.existsByCpf(cpf)) throw new ConflictException("CPF já cadastrado.");
        if (users.existsByPhone(phone)) throw new ConflictException("Telefone já cadastrado.");

        User user = new User();
        user.setName(request.name().trim());
        user.setCpf(cpf);
        user.setEmail(email);
        user.setPhone(phone);
        user.setBirthDate(request.birthDate());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        users.save(user);

        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setAccountNumber(generator.next());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        account.setYieldLabel("Conta corrente Via");
        accounts.save(account);

        createPixKey(account, PixKeyType.CPF, cpf);
        createPixKey(account, PixKeyType.EMAIL, email);
        createPixKey(account, PixKeyType.PHONE, phone);

        UserSettings prefs = new UserSettings();
        prefs.setUser(user);
        prefs.setCompactMode(false);
        prefs.setNotifications(true);
        prefs.setHideBalance(false);
        settings.save(prefs);

        Card card = new Card();
        card.setAccount(account);
        card.setType("DEBIT");
        card.setBrand("VIA");
        card.setLast4(generator.last4());
        card.setHolderName(user.getName().toUpperCase(Locale.ROOT));
        card.setExpiration(YearMonth.now().plusYears(5).format(DateTimeFormatter.ofPattern("MM/yy")));
        card.setBlocked(false);
        card.setLimitAmount(BigDecimal.ZERO);
        card.setUsedLimit(BigDecimal.ZERO);
        card.setProductName("Essencial");
        cards.save(card);

        String token = jwtService.generateToken(user.getId());
        return authResponse(user, token, "Conta criada com sucesso.");
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        var auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.password())
        );
        User user = users.findByEmailIgnoreCase(auth.getName())
            .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        String token = jwtService.generateToken(user.getId());
        return authResponse(user, token, "Login realizado com sucesso.");
    }

    @Transactional
    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) return;
        String token = bearerToken.substring(7);
        String jti = jwtService.getJti(token);
        if (jti == null || revokedTokens.existsByJti(jti)) return;
        RevokedToken revoked = new RevokedToken();
        revoked.setJti(jti);
        revoked.setExpiresAt(jwtService.getExpiration(token));
        revokedTokens.save(revoked);
    }

    private void createPixKey(BankAccount account, PixKeyType type, String value) {
        PixKey key = new PixKey();
        key.setAccount(account); key.setType(type); key.setValue(value);
        pixKeys.save(key);
    }

    private AuthResponse authResponse(User user, String token, String message) {
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(),
            new AuthResponse.UserSummary(user.getId(), user.getName(), user.getEmail()), message);
    }
}
