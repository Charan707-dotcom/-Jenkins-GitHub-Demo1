package com.banking.bankingsystem.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banking.bankingsystem.bank.BankAccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private BankAccount account;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    private LocalDateTime transactionTime;

    // Default constructor
    public Transaction() {
        this.transactionTime = LocalDateTime.now();
    }

    // Constructor
    public Transaction(
            BankAccount account,
            String type,
            BigDecimal amount,
            BigDecimal balanceAfter) {

        this.account = account;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.transactionTime = LocalDateTime.now();
    }

    // Getters

    public Long getId() {
        return id;
    }

    public BankAccount getAccount() {
        return account;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }
}