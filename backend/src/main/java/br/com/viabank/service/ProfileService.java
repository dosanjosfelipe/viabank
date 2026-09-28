package br.com.viabank.service;

import br.com.viabank.domain.PixKeyType;
import br.com.viabank.dto.profile.ProfileDtos.*;
import br.com.viabank.entity.*;
import br.com.viabank.exception.ConflictException;
import br.com.viabank.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class ProfileService {
    private final CurrentUserService currentUser;
    private final UserRepository users;
    private final BankAccountRepository accounts;
    private final PixKeyRepository pixKeys;

    public ProfileService(CurrentUserService currentUser, UserRepository users,
                          BankAccountRepository accounts, PixKeyRepository pixKeys) {
        this.currentUser = currentUser; this.users = users; this.accounts = accounts; this.pixKeys = pixKeys;
    }

    public ProfileResponse get() { return map(currentUser.get()); }

    @Transactional
    public ProfileResponse update(UpdateProfileRequest request) {
        User user = currentUser.get();
        BankAccount account = accounts.findByUserId(user.getId()).orElseThrow();

        if (request.email() != null && !request.email().isBlank()) {
            String email = request.email().trim().toLowerCase(Locale.ROOT);
            if (!email.equalsIgnoreCase(user.getEmail()) && users.existsByEmailIgnoreCase(email))
                throw new ConflictException("E-mail já cadastrado.");
            user.setEmail(email);
            pixKeys.findByAccountIdAndType(account.getId(), PixKeyType.EMAIL).ifPresent(k -> k.setValue(email));
        }

        if (request.phone() != null && !request.phone().isBlank()) {
            String phone = request.phone().replaceAll("\\D", "");
            if (!phone.equals(user.getPhone()) && users.existsByPhone(phone))
                throw new ConflictException("Telefone já cadastrado.");
            user.setPhone(phone);
            pixKeys.findByAccountIdAndType(account.getId(), PixKeyType.PHONE).ifPresent(k -> k.setValue(phone));
        }

        if (request.name() != null && !request.name().isBlank()) user.setName(request.name().trim());
        if (request.address() != null) user.setAddress(blankToNull(request.address()));
        if (request.city() != null) user.setCity(blankToNull(request.city()));
        if (request.state() != null) user.setState(blankToNull(request.state()) == null ? null : request.state().trim().toUpperCase(Locale.ROOT));
        if (request.zipCode() != null) user.setZipCode(request.zipCode().replaceAll("\\D", ""));

        return map(users.save(user));
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private ProfileResponse map(User user) {
        return new ProfileResponse(
            user.getId(), user.getName(), maskCpf(user.getCpf()), user.getEmail(), user.getPhone(),
            user.getBirthDate(), user.getAddress(), user.getCity(), user.getState(), user.getZipCode(),
            user.getCreatedAt()
        );
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return "***.***.***-**";
        return "***.***." + cpf.substring(6, 9) + "-**";
    }
}
