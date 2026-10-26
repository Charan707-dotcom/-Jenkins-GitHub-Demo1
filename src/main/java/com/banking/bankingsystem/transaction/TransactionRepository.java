package com.banking.bankingsystem.transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banking.bankingsystem.bank.BankAccount;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountOrderByTransactionTimeDesc(
            BankAccount account
    );
}
