package br.com.viabank.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "devices")
public class Device {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(length = 30)
    private String language;

    @Column(length = 100)
    private String platform;

    @Column(name = "screen_width")
    private Integer screenWidth;

    @Column(name = "screen_height")
    private Integer screenHeight;

    @Column(length = 80)
    private String timezone;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @PrePersist @PreUpdate
    void touch() { lastSeenAt = Instant.now(); }

    public void setUser(User user) { this.user = user; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public void setLanguage(String language) { this.language = language; }
    public void setPlatform(String platform) { this.platform = platform; }
    public void setScreenWidth(Integer screenWidth) { this.screenWidth = screenWidth; }
    public void setScreenHeight(Integer screenHeight) { this.screenHeight = screenHeight; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}
