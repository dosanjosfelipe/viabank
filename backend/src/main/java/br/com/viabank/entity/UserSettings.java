package br.com.viabank.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "compact_mode", nullable = false)
    private boolean compactMode;

    @Column(nullable = false)
    private boolean notifications = true;

    @Column(name = "hide_balance", nullable = false)
    private boolean hideBalance;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public boolean isCompactMode() { return compactMode; }
    public boolean isNotifications() { return notifications; }
    public boolean isHideBalance() { return hideBalance; }

    public void setUser(User user) { this.user = user; }
    public void setCompactMode(boolean compactMode) { this.compactMode = compactMode; }
    public void setNotifications(boolean notifications) { this.notifications = notifications; }
    public void setHideBalance(boolean hideBalance) { this.hideBalance = hideBalance; }
}
