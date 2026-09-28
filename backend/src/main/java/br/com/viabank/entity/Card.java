package br.com.viabank.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cards")
public class Card {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount account;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 30)
    private String brand;

    @Column(nullable = false, length = 4)
    private String last4;

    @Column(name = "holder_name", nullable = false, length = 120)
    private String holderName;

    @Column(nullable = false, length = 5)
    private String expiration;

    @Column(nullable = false)
    private boolean blocked;

    @Column(name = "limit_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal limitAmount = BigDecimal.ZERO;

    @Column(name = "used_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal usedLimit = BigDecimal.ZERO;

    @Column(name = "product_name", nullable = false, length = 80)
    private String productName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist void prePersist() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public BankAccount getAccount() { return account; }
    public String getType() { return type; }
    public String getBrand() { return brand; }
    public String getLast4() { return last4; }
    public String getHolderName() { return holderName; }
    public String getExpiration() { return expiration; }
    public boolean isBlocked() { return blocked; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public BigDecimal getUsedLimit() { return usedLimit; }
    public String getProductName() { return productName; }

    public void setAccount(BankAccount account) { this.account = account; }
    public void setType(String type) { this.type = type; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setLast4(String last4) { this.last4 = last4; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public void setExpiration(String expiration) { this.expiration = expiration; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
    public void setUsedLimit(BigDecimal usedLimit) { this.usedLimit = usedLimit; }
    public void setProductName(String productName) { this.productName = productName; }
}
