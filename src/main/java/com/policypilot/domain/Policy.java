package com.policypilot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "policies")
public class Policy {

    @Id
    private String policyNumber;
    private String holderName;
    private String holderEmail;
    private String holderPhone;
    private String policyType;
    private BigDecimal sumInsured;
    private BigDecimal premiumAmount;
    private LocalDate premiumDueDate;
    private String status;

    protected Policy() {
    }

    public String getPolicyNumber() { return policyNumber; }
    public String getHolderName() { return holderName; }
    public String getHolderEmail() { return holderEmail; }
    public String getHolderPhone() { return holderPhone; }
    public String getPolicyType() { return policyType; }
    public BigDecimal getSumInsured() { return sumInsured; }
    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public LocalDate getPremiumDueDate() { return premiumDueDate; }
    public String getStatus() { return status; }
}
