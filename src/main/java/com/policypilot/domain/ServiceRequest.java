package com.policypilot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String policyNumber;
    @Column(length = 1000)
    private String issue;
    private String status;
    private Instant createdAt;

    protected ServiceRequest() {
    }

    public ServiceRequest(String policyNumber, String issue) {
        this.policyNumber = policyNumber;
        this.issue = issue;
        this.status = "OPEN";
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getPolicyNumber() { return policyNumber; }
    public String getIssue() { return issue; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
