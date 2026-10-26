package com.banking.bankingsystem.loan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.banking.bankingsystem.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loanNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String loanType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principal;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal annualRate;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "PENDING";

    // Default constructor
    public Loan() {
    }

    // Constructor
    public Loan(
            String loanNumber,
            User user,
            String loanType,
            BigDecimal principal,
            BigDecimal annualRate,
            LocalDate startDate) {

        this.loanNumber = loanNumber;
        this.user = user;
        this.loanType = loanType;
        this.principal = principal;
        this.annualRate = annualRate;
        this.startDate = startDate;
    }

    // Calculate interest
    public BigDecimal interest() {

        if (status.equals("PENDING")) {
            return BigDecimal.ZERO;
        }

        long days = Math.max(
                0,
                ChronoUnit.DAYS.between(
                        startDate,
                        LocalDate.now()
                )
        );

        return principal
                .multiply(annualRate)
                .divide(
                        BigDecimal.valueOf(100),
                        10,
                        RoundingMode.HALF_UP
                )
                .multiply(
                        BigDecimal.valueOf(days)
                )
                .divide(
                        BigDecimal.valueOf(365),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    // Calculate remaining amount
    public BigDecimal totalDue() {

        return principal
                .add(interest())
                .subtract(amountPaid)
                .max(BigDecimal.ZERO)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public User getUser() {
        return user;
    }

    public String getLoanType() {
        return loanType;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public String getStatus() {
        return status;
    }

    // Setters

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    // Add loan payment
    public void addPayment(BigDecimal payment) {

        amountPaid = amountPaid.add(payment);

        if (totalDue().compareTo(BigDecimal.ZERO) <= 0) {
            status = "PAID";
        }
    }
}