package com.policypilot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    private String claimId;
    private String policyNumber;
    private String claimType;
    private BigDecimal amount;
    private String status;
    private LocalDate filedOn;
    private String lastUpdate;

    protected Claim() {
    }

    public String getClaimId() { return claimId; }
    public String getPolicyNumber() { return policyNumber; }
    public String getClaimType() { return claimType; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDate getFiledOn() { return filedOn; }
    public String getLastUpdate() { return lastUpdate; }
}
