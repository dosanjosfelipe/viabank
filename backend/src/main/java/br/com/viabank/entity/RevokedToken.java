package br.com.viabank.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "revoked_tokens",
    uniqueConstraints = @UniqueConstraint(name = "uk_revoked_tokens_jti", columnNames = "jti"))
public class RevokedToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    @PrePersist void prePersist() { revokedAt = Instant.now(); }

    public String getJti() { return jti; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setJti(String jti) { this.jti = jti; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
