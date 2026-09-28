package br.com.viabank.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "pix_favorites")
public class PixFavorite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "pix_key", nullable = false, length = 180)
    private String pixKey;

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist void prePersist() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public String getPixKey() { return pixKey; }
    public String getBankName() { return bankName; }
    public Instant getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setName(String name) { this.name = name; }
    public void setPixKey(String pixKey) { this.pixKey = pixKey; }
    public void setBankName(String bankName) { this.bankName = bankName; }
}
