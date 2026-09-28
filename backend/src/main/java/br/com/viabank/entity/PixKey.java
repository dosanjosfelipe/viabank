package br.com.viabank.entity;

import br.com.viabank.domain.PixKeyType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "pix_keys",
    uniqueConstraints = @UniqueConstraint(name = "uk_pix_keys_value", columnNames = "key_value"))
public class PixKey {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount account;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 20)
    private PixKeyType type;

    @Column(name = "key_value", nullable = false, length = 180)
    private String value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.Instant createdAt;

    @PrePersist
    void prePersist() { createdAt = java.time.Instant.now(); }

    public Long getId() { return id; }
    public BankAccount getAccount() { return account; }
    public PixKeyType getType() { return type; }
    public String getValue() { return value; }
    public Instant getCreatedAt() { return createdAt; }

    public void setAccount(BankAccount account) { this.account = account; }
    public void setType(PixKeyType type) { this.type = type; }
    public void setValue(String value) { this.value = value; }
}
