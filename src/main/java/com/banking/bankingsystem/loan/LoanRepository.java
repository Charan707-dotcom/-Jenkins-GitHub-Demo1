package com.banking.bankingsystem.loan;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banking.bankingsystem.user.User;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserOrderByIdDesc(User user);

    List<Loan> findAllByOrderByIdDesc();
}
