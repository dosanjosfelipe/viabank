package br.com.viabank.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "investments")
public class Investment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private InvestmentProduct product;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principal;

    @Column(name = "current_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "monthly_return", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyReturn = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist void prePersist() {
        Instant now = Instant.now();
        createdAt = now; updatedAt = now;
    }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public BankAccount getAccount() { return account; }
    public InvestmentProduct getProduct() { return product; }
    public BigDecimal getPrincipal() { return principal; }
    public BigDecimal getCurrentValue() { return currentValue; }
    public BigDecimal getMonthlyReturn() { return monthlyReturn; }

    public void setAccount(BankAccount account) { this.account = account; }
    public void setProduct(InvestmentProduct product) { this.product = product; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }
    public void setMonthlyReturn(BigDecimal monthlyReturn) { this.monthlyReturn = monthlyReturn; }
}
