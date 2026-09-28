package br.com.viabank.service;

import br.com.viabank.domain.*;
import br.com.viabank.dto.pix.PixDtos.*;
import br.com.viabank.entity.*;
import br.com.viabank.exception.*;
import br.com.viabank.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class PixService {
    private final CurrentUserService currentUser;
    private final BankAccountRepository accounts;
    private final PixKeyRepository pixKeys;
    private final BankTransactionRepository transactions;
    private final PixFavoriteRepository favorites;

    public PixService(CurrentUserService currentUser, BankAccountRepository accounts,
                      PixKeyRepository pixKeys, BankTransactionRepository transactions,
                      PixFavoriteRepository favorites) {
        this.currentUser = currentUser; this.accounts = accounts; this.pixKeys = pixKeys;
        this.transactions = transactions; this.favorites = favorites;
    }

    public List<PixKeyResponse> keys() {
        BankAccount account = account();
        return pixKeys.findAllByAccountIdOrderByIdAsc(account.getId()).stream().map(this::mapKey).toList();
    }

    @Transactional
    public PixKeyResponse createKey(CreateKeyRequest request) {
        BankAccount account = account();
        PixKeyType type;
        try { type = PixKeyType.valueOf(request.type().trim().toUpperCase(Locale.ROOT)); }
        catch (Exception e) { throw new BusinessException("Tipo de chave PIX inválido."); }

        String value;
        if (type == PixKeyType.RANDOM) value = UUID.randomUUID().toString();
        else {
            if (request.value() == null || request.value().isBlank())
                throw new BusinessException("Valor da chave PIX é obrigatório.");
            value = normalizeKey(type, request.value());
        }

        if (pixKeys.existsByValue(value)) throw new ConflictException("Chave PIX já cadastrada.");
        PixKey key = new PixKey();
        key.setAccount(account); key.setType(type); key.setValue(value);
        return mapKey(pixKeys.save(key));
    }

    @Transactional
    public void deleteKey(Long id) {
        BankAccount account = account();
        PixKey key = pixKeys.findById(id).orElseThrow(() -> new NotFoundException("Chave PIX não encontrada."));
        if (!key.getAccount().getId().equals(account.getId()))
            throw new ForbiddenException("Esta chave PIX não pertence à sua conta.");
        pixKeys.delete(key);
    }

    @Transactional
    public PixTransferResponse send(SendPixRequest request) {
        BankAccount senderBase = account();
        PixKey destinationKey = resolveKey(request.key());
        Long receiverId = destinationKey.getAccount().getId();

        if (senderBase.getId().equals(receiverId))
            throw new BusinessException("Não é possível enviar PIX para a própria conta.");

        Long firstId = Math.min(senderBase.getId(), receiverId);
        Long secondId = Math.max(senderBase.getId(), receiverId);

        BankAccount first = accounts.findByIdForUpdate(firstId)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada."));
        BankAccount second = accounts.findByIdForUpdate(secondId)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada."));

        BankAccount sender = senderBase.getId().equals(first.getId()) ? first : second;
        BankAccount receiver = receiverId.equals(first.getId()) ? first : second;

        if (sender.getStatus() != AccountStatus.ACTIVE) throw new ForbiddenException("Sua conta não está ativa.");
        if (receiver.getStatus() != AccountStatus.ACTIVE) throw new BusinessException("A conta de destino não está ativa.");

        BigDecimal amount = AccountService.money(request.amount());
        if (sender.getBalance().compareTo(amount) < 0) throw new BusinessException("Saldo insuficiente.");

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        String reference = UUID.randomUUID().toString();
        String description = request.description() == null ? "" : request.description().trim();

        BankTransaction debit = new BankTransaction();
        debit.setAccount(sender); debit.setCounterpartyAccount(receiver);
        debit.setType(TransactionType.PIX_SENT); debit.setAmount(amount.negate());
        debit.setName("PIX enviado");
        debit.setDescription(description.isBlank() ? "Para " + receiver.getUser().getName() : description);
        debit.setCategory("PIX"); debit.setTransferReference(reference);

        BankTransaction credit = new BankTransaction();
        credit.setAccount(receiver); credit.setCounterpartyAccount(sender);
        credit.setType(TransactionType.PIX_RECEIVED); credit.setAmount(amount);
        credit.setName("PIX recebido");
        credit.setDescription(description.isBlank() ? "De " + sender.getUser().getName() : description);
        credit.setCategory("PIX"); credit.setTransferReference(reference);

        transactions.saveAll(List.of(debit, credit));
        return new PixTransferResponse(reference, amount, receiver.getUser().getName(), Instant.now(), "completed");
    }

    public List<FavoriteResponse> favorites() {
        User user = currentUser.get();
        return favorites.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(f -> new FavoriteResponse(f.getId(), f.getName(), f.getPixKey(), mask(f.getPixKey()), f.getBankName()))
            .toList();
    }

    @Transactional
    public FavoriteResponse addFavorite(AddFavoriteRequest request) {
        User user = currentUser.get();
        PixFavorite f = new PixFavorite();
        f.setUser(user); f.setName(request.name().trim()); f.setPixKey(request.pixKey().trim());
        f.setBankName(request.bankName() == null || request.bankName().isBlank() ? "Via Bank" : request.bankName().trim());
        favorites.save(f);
        return new FavoriteResponse(f.getId(), f.getName(), f.getPixKey(), mask(f.getPixKey()), f.getBankName());
    }

    private PixKey resolveKey(String raw) {
        String value = raw.trim();
        Optional<PixKey> exact = pixKeys.findByValue(value);
        if (exact.isPresent()) return exact.get();

        Optional<PixKey> lower = pixKeys.findByValue(value.toLowerCase(Locale.ROOT));
        if (lower.isPresent()) return lower.get();

        String digits = value.replaceAll("\\D", "");
        if (!digits.isBlank()) {
            Optional<PixKey> numeric = pixKeys.findByValue(digits);
            if (numeric.isPresent()) return numeric.get();
        }
        throw new NotFoundException("Chave PIX não encontrada.");
    }

    private BankAccount account() {
        User user = currentUser.get();
        return accounts.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("Conta não encontrada."));
    }

    private PixKeyResponse mapKey(PixKey key) {
        return new PixKeyResponse(key.getId(), key.getType().name().toLowerCase(), key.getValue(), mask(key.getValue()));
    }

    private String normalizeKey(PixKeyType type, String value) {
        return switch (type) {
            case EMAIL -> value.trim().toLowerCase(Locale.ROOT);
            case CPF, PHONE -> value.replaceAll("\\D", "");
            case RANDOM -> value.trim();
        };
    }

    private String mask(String value) {
        if (value == null || value.length() <= 4) return "••••";
        return value.substring(0, Math.min(3, value.length())) + "••••" + value.substring(value.length() - 2);
    }
}
