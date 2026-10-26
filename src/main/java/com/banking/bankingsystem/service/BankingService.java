package com.banking.bankingsystem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banking.bankingsystem.bank.BankAccount;
import com.banking.bankingsystem.bank.BankAccountRepository;
import com.banking.bankingsystem.loan.Loan;
import com.banking.bankingsystem.loan.LoanRepository;
import com.banking.bankingsystem.transaction.Transaction;
import com.banking.bankingsystem.transaction.TransactionRepository;
import com.banking.bankingsystem.user.User;
import com.banking.bankingsystem.user.UserRepository;

@Service
public class BankingService {

    private final UserRepository users;
    private final BankAccountRepository accounts;
    private final TransactionRepository transactions;
    private final LoanRepository loans;

    public BankingService(
            UserRepository users,
            BankAccountRepository accounts,
            TransactionRepository transactions,
            LoanRepository loans) {

        this.users = users;
        this.accounts = accounts;
        this.transactions = transactions;
        this.loans = loans;
    }

    // =========================
    // USER REGISTRATION
    // =========================

    public User register(
            String name,
            int age,
            String phone,
            String address,
            String username,
            String password) {

        if (!name.matches("[A-Za-z ]+")) {
            throw new IllegalArgumentException(
                    "Name can contain alphabets and spaces only."
            );
        }

        if (age < 1 || age > 100) {
            throw new IllegalArgumentException(
                    "Age must be between 1 and 100."
            );
        }

        if (!phone.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "Phone number must contain exactly 10 digits."
            );
        }

        if (users.existsByPhone(phone)) {
            throw new IllegalArgumentException(
                    "Phone number already exists."
            );
        }

        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists."
            );
        }

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Address is required."
            );
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Password is required."
            );
        }

        User user = new User(
                name.trim(),
                age,
                phone,
                address.trim(),
                username.trim(),
                password
        );

        return users.save(user);
    }

    // =========================
    // USER LOGIN
    // =========================

    public User login(
            String username,
            String password) {

        User user = users
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid username or password."
                        )
                );

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException(
                    "Invalid username or password."
            );
        }

        return user;
    }

    // =========================
    // FIND USER ACCOUNT
    // =========================

    public Optional<BankAccount> account(User user) {

        return accounts.findByUserId(user.getId());
    }

    // =========================
    // CREATE BANK ACCOUNT
    // =========================

    @Transactional
    public BankAccount createAccount(
            User user,
            String accountType,
            BigDecimal initialDeposit) {

        if (user.getAge() < 18) {
            throw new IllegalArgumentException(
                    "Account creation requires age 18 or above."
            );
        }

        if (initialDeposit == null
                || initialDeposit.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Initial deposit cannot be negative."
            );
        }

        if (accounts.findByUserId(user.getId())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "You already have an account."
            );
        }

        String accountNumber =
                "SB"
                + String.format(
                        "%08d",
                        System.currentTimeMillis()
                                % 100000000
                );

        while (accounts
                .findByAccountNumber(accountNumber)
                .isPresent()) {

            accountNumber =
                    "SB"
                    + String.format(
                            "%08d",
                            (
                                    System.currentTimeMillis()
                                    + new Random().nextInt(9999)
                            ) % 100000000
                    );
        }

        BankAccount account =
                new BankAccount(
                        accountNumber,
                        accountType,
                        initialDeposit.setScale(2),
                        user
                );

        accounts.save(account);

        if (initialDeposit.compareTo(
                BigDecimal.ZERO) > 0) {

            Transaction transaction =
                    new Transaction(
                            account,
                            "INITIAL DEPOSIT",
                            initialDeposit,
                            account.getBalance()
                    );

            transactions.save(transaction);
        }

        return account;
    }

    // =========================
    // DEPOSIT
    // =========================

    @Transactional
    public void deposit(
            User user,
            BigDecimal amount) {

        changeBalance(
                user,
                amount,
                "DEPOSIT"
        );
    }

    // =========================
    // WITHDRAW
    // =========================

    @Transactional
    public void withdraw(
            User user,
            BigDecimal amount) {

        if (amount == null) {
            throw new IllegalArgumentException(
                    "Amount is required."
            );
        }

        changeBalance(
                user,
                amount.negate(),
                "WITHDRAWAL"
        );
    }

    // =========================
    // CHANGE BALANCE
    // =========================

    private void changeBalance(
            User user,
            BigDecimal amount,
            String transactionType) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) == 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero."
            );
        }

        BankAccount account =
                accounts
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Create an account first."
                                )
                        );

        BigDecimal actualAmount =
                amount.abs();

        if (transactionType.equals("WITHDRAWAL")
                && account.getBalance()
                        .compareTo(actualAmount) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient balance."
            );
        }

        BigDecimal newBalance =
                account.getBalance()
                        .add(amount);

        account.setBalance(newBalance);

        accounts.save(account);

        Transaction transaction =
                new Transaction(
                        account,
                        transactionType,
                        actualAmount,
                        newBalance
                );

        transactions.save(transaction);
    }

    // =========================
    // TRANSACTION HISTORY
    // =========================

    public List<Transaction> transactions(
            User user) {

        return account(user)
                .map(
                        transactions::
                        findByAccountOrderByTransactionTimeDesc
                )
                .orElse(List.of());
    }

    // =========================
    // LOAN INTEREST RATES
    // =========================

    public BigDecimal rate(String loanType) {

        return switch (loanType) {

            case "PERSONAL" ->
                    BigDecimal.valueOf(12);

            case "HOME" ->
                    BigDecimal.valueOf(7);

            case "EDUCATION" ->
                    BigDecimal.valueOf(6);

            case "VEHICLE" ->
                    BigDecimal.valueOf(9);

            case "BUSINESS" ->
                    BigDecimal.valueOf(11);

            default ->
                    throw new IllegalArgumentException(
                            "Invalid loan type."
                    );
        };
    }

    // =========================
    // APPLY FOR LOAN
    // =========================

    public Loan applyLoan(
            User user,
            String loanType,
            BigDecimal amount,
            LocalDate startDate) {

        if (account(user).isEmpty()) {

            throw new IllegalArgumentException(
                    "Create a bank account first."
            );
        }

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Loan amount must be greater than zero."
            );
        }

        if (startDate.isAfter(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Loan date cannot be in the future."
            );
        }

        String loanNumber =
                "LN" + System.currentTimeMillis();

        Loan loan =
                new Loan(
                        loanNumber,
                        user,
                        loanType,
                        amount.setScale(2),
                        rate(loanType),
                        startDate
                );

        return loans.save(loan);
    }

    // =========================
    // USER LOANS
    // =========================

    public List<Loan> userLoans(User user) {

        return loans.findByUserOrderByIdDesc(user);
    }

    // =========================
    // ALL LOANS
    // =========================

    public List<Loan> allLoans() {

        return loans.findAllByOrderByIdDesc();
    }

    // =========================
    // SANCTION LOAN
    // =========================

    @Transactional
    public void sanction(Long loanId) {

        Loan loan =
                loans.findById(loanId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Loan not found."
                                )
                        );

        loan.setStatus("APPROVED");

        loans.save(loan);
    }

    // =========================
    // REJECT LOAN
    // =========================

    @Transactional
    public void reject(Long loanId) {

        Loan loan =
                loans.findById(loanId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Loan not found."
                                )
                        );

        loan.setStatus("REJECTED");

        loans.save(loan);
    }

    // =========================
    // PAY LOAN
    // =========================

    @Transactional
    public void payLoan(
            User user,
            Long loanId,
            BigDecimal amount) {

        Loan loan =
                loans.findById(loanId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Loan not found."
                                )
                        );

        if (!loan.getUser()
                .getId()
                .equals(user.getId())) {

            throw new IllegalArgumentException(
                    "Invalid loan."
            );
        }

        if (!loan.getStatus().equals("APPROVED")) {

            throw new IllegalArgumentException(
                    "Loan is not approved."
            );
        }

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.compareTo(
                        loan.totalDue()) > 0) {

            throw new IllegalArgumentException(
                    "Invalid payment amount."
            );
        }

        BankAccount account =
                accounts
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Bank account not found."
                                )
                        );

        if (account.getBalance()
                .compareTo(amount) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient account balance."
            );
        }

        BigDecimal newBalance =
                account.getBalance()
                        .subtract(amount);

        account.setBalance(newBalance);

        accounts.save(account);

        loan.addPayment(amount);

        loans.save(loan);

        Transaction transaction =
                new Transaction(
                        account,
                        "LOAN PAYMENT",
                        amount,
                        newBalance
                );

        transactions.save(transaction);
    }

    // =========================
    // ACCOUNT TO ACCOUNT TRANSFER
    // =========================

    @Transactional
    public void transfer(
            User sender,
            String receiverAccountNumber,
            BigDecimal amount) {

        // Validate amount
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero."
            );
        }

        // Validate receiver account number
        if (receiverAccountNumber == null
                || receiverAccountNumber.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Receiver account number is required."
            );
        }

        // Find sender account
        BankAccount senderAccount =
                accounts.findByUserId(sender.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Sender bank account not found."
                                )
                        );

        // Find receiver account
        BankAccount receiverAccount =
                accounts.findByAccountNumber(
                        receiverAccountNumber.trim()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Receiver account not found."
                        )
                );

        // Prevent transfer to own account
        if (senderAccount.getId()
                .equals(receiverAccount.getId())) {

            throw new IllegalArgumentException(
                    "You cannot transfer money to your own account."
            );
        }

        // Check balance
        if (senderAccount.getBalance()
                .compareTo(amount) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient balance."
            );
        }

        // New sender balance
        BigDecimal senderNewBalance =
                senderAccount.getBalance()
                        .subtract(amount);

        // New receiver balance
        BigDecimal receiverNewBalance =
                receiverAccount.getBalance()
                        .add(amount);

        // Update sender
        senderAccount.setBalance(senderNewBalance);

        // Update receiver
        receiverAccount.setBalance(receiverNewBalance);

        // Save both
        accounts.save(senderAccount);
        accounts.save(receiverAccount);

        // Sender transaction
        Transaction senderTransaction =
                new Transaction(
                        senderAccount,
                        "TRANSFER SENT",
                        amount,
                        senderNewBalance
                );

        transactions.save(senderTransaction);

        // Receiver transaction
        Transaction receiverTransaction =
                new Transaction(
                        receiverAccount,
                        "TRANSFER RECEIVED",
                        amount,
                        receiverNewBalance
                );

        transactions.save(receiverTransaction);
    }

    // =========================
    // BANK DASHBOARD COUNTS
    // =========================

    public long userCount() {

        return users.count();
    }

    public long accountCount() {

        return accounts.count();
    }
    public boolean hasSufficientBalance(
        double balance, double amount) {
    return balance >= amount && amount > 0;
}
}