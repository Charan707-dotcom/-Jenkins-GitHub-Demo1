package com.banking.bankingsystem.bank;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByUserId(Long userId);

    Optional<BankAccount> findByAccountNumber(String accountNumber);
}