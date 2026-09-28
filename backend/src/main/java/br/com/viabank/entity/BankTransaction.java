package br.com.viabank.entity;

import br.com.viabank.domain.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions",
    indexes = {
        @Index(name = "idx_transactions_account_created", columnList = "account_id,created_at"),
        @Index(name = "idx_transactions_transfer_ref", columnList = "transfer_reference")
    })
public class BankTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_account_id")
    private BankAccount counterpartyAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 80)
    private String category;

    @Column(name = "transfer_reference", length = 36)
    private String transferReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        if (transferReference == null) transferReference = UUID.randomUUID().toString();
    }

    public Long getId() { return id; }
    public BankAccount getAccount() { return account; }
    public BankAccount getCounterpartyAccount() { return counterpartyAccount; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getTransferReference() { return transferReference; }
    public Instant getCreatedAt() { return createdAt; }

    public void setAccount(BankAccount account) { this.account = account; }
    public void setCounterpartyAccount(BankAccount counterpartyAccount) { this.counterpartyAccount = counterpartyAccount; }
    public void setType(TransactionType type) { this.type = type; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setTransferReference(String transferReference) { this.transferReference = transferReference; }
}
