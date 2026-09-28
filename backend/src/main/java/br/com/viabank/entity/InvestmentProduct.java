package br.com.viabank.entity;

import br.com.viabank.domain.RiskLevel;
import jakarta.persistence.*;

@Entity
@Table(name = "investment_products")
public class InvestmentProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel risk;

    @Column(name = "return_label", nullable = false, length = 120)
    private String returnLabel;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public RiskLevel getRisk() { return risk; }
    public String getReturnLabel() { return returnLabel; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setRisk(RiskLevel risk) { this.risk = risk; }
    public void setReturnLabel(String returnLabel) { this.returnLabel = returnLabel; }
    public void setActive(boolean active) { this.active = active; }
}
