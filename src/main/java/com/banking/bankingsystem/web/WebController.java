package com.banking.bankingsystem.web;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.banking.bankingsystem.service.BankingService;
import com.banking.bankingsystem.user.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

    private final BankingService service;

    public WebController(BankingService service) {
        this.service = service;
    }

    // =========================================================
    // HOME
    // =========================================================

    @GetMapping("/")
    public String home() {
        return "index";
    }

    // =========================================================
    // USER REGISTRATION
    // =========================================================

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam int age,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        try {

            service.register(
                    name,
                    age,
                    phone,
                    address,
                    username,
                    password
            );

            model.addAttribute(
                    "success",
                    "Registration successful. Please login."
            );

            return "login";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "register";
        }
    }

    // =========================================================
    // USER LOGIN
    // =========================================================

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        try {

            User user =
                    service.login(
                            username,
                            password
                    );

            session.setAttribute(
                    "user",
                    user
            );

            return "redirect:/user/dashboard";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "login";
        }
    }

    // =========================================================
    // GET LOGGED-IN USER
    // =========================================================

    private User getUser(HttpSession session) {

        return (User) session.getAttribute("user");
    }

    // =========================================================
    // USER DASHBOARD
    // =========================================================

    @GetMapping("/user/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        User user = getUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "user",
                user
        );

        model.addAttribute(
                "account",
                service.account(user).orElse(null)
        );

        model.addAttribute(
                "loans",
                service.userLoans(user)
        );

        return "user-dashboard";
    }

    // =========================================================
    // CREATE BANK ACCOUNT PAGE
    // =========================================================

    @GetMapping("/account/create")
    public String accountPage(
            HttpSession session,
            Model model) {

        User user = getUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        return "account";
    }

    // =========================================================
    // CREATE BANK ACCOUNT
    // =========================================================

    @PostMapping("/account/create")
    public String createAccount(
            @RequestParam String accountType,
            @RequestParam BigDecimal initialDeposit,
            HttpSession session,
            Model model) {

        User user = getUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {

            service.createAccount(
                    user,
                    accountType,
                    initialDeposit
            );

            return "redirect:/user/dashboard";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "account";
        }
    }

    // =========================================================
    // DEPOSIT
    // =========================================================

    @PostMapping("/account/deposit")
    public String deposit(
            @RequestParam BigDecimal amount,
            HttpSession session,
            Model model) {

        return money(
                amount,
                session,
                model,
                true
        );
    }

    // =========================================================
    // WITHDRAW
    // =========================================================

    @PostMapping("/account/withdraw")
    public String withdraw(
            @RequestParam BigDecimal amount,
            HttpSession session,
            Model model) {

        return money(
                amount,
                session,
                model,
                false
        );
    }

    // =========================================================
    // DEPOSIT / WITHDRAW HELPER
    // =========================================================

    private String money(
            BigDecimal amount,
            HttpSession session,
            Model model,
            boolean deposit) {

        User user = getUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {

            if (deposit) {

                service.deposit(
                        user,
                        amount
                );

            } else {

                service.withdraw(
                        user,
                        amount
                );
            }

            return "redirect:/user/dashboard";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "user",
                    user
            );

            model.addAttribute(
                    "account",
                    service.account(user).orElse(null)
            );

            model.addAttribute(
                    "loans",
                    service.userLoans(user)
            );

            return "user-dashboard";
        }
    }

    // =========================================================
    // TRANSACTION HISTORY
    // =========================================================

    @GetMapping("/transactions")
    public String transactions(
            HttpSession session,
            Model model) {

        User user = getUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "transactions",
                service.transactions(user)
        );

        return "transactions";
    }

    // =========================================================
    // LOAN APPLICATION PAGE
    // =========================================================

    @GetMapping("/loans/apply")
    public String loanPage(
            HttpSession session,
            Model model) {

        User user = getUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        return "apply-loan";
    }

    // =========================================================
    // APPLY FOR LOAN
    // =========================================================

    @PostMapping("/loans/apply")
    public String applyLoan(
            @RequestParam String loanType,
            @RequestParam BigDecimal amount,
            @RequestParam LocalDate startDate,
            HttpSession session,
            Model model) {

        User user = getUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {

            service.applyLoan(
                    user,
                    loanType,
                    amount,
                    startDate
            );

            return "redirect:/user/dashboard";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "apply-loan";
        }
    }

    // =========================================================
    // PAY LOAN
    // =========================================================

    @PostMapping("/loans/pay")
    public String payLoan(
            @RequestParam Long loanId,
            @RequestParam BigDecimal amount,
            HttpSession session,
            Model model) {

        User user = getUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {

            service.payLoan(
                    user,
                    loanId,
                    amount
            );

            return "redirect:/user/dashboard";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "user",
                    user
            );

            model.addAttribute(
                    "account",
                    service.account(user).orElse(null)
            );

            model.addAttribute(
                    "loans",
                    service.userLoans(user)
            );

            return "user-dashboard";
        }
    }

    // =========================================================
    // ACCOUNT TO ACCOUNT TRANSFER PAGE
    // =========================================================

    @GetMapping("/user/transfer")
    public String transferPage(
            HttpSession session,
            Model model) {

        User user = getUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "user",
                user
        );

        model.addAttribute(
                "account",
                service.account(user).orElse(null)
        );

        return "transfer";
    }

    // =========================================================
    // ACCOUNT TO ACCOUNT TRANSFER
    // =========================================================

    @PostMapping("/user/transfer")
    public String transfer(
            @RequestParam String receiverAccountNumber,
            @RequestParam BigDecimal amount,
            HttpSession session,
            Model model) {

        User user = getUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        try {

            service.transfer(
                    user,
                    receiverAccountNumber,
                    amount
            );

            return "redirect:/user/dashboard?transferSuccess=true";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "user",
                    user
            );

            model.addAttribute(
                    "account",
                    service.account(user).orElse(null)
            );

            model.addAttribute(
                    "loans",
                    service.userLoans(user)
            );

            return "user-dashboard";
        }
    }

    // =========================================================
    // BANK ADMIN LOGIN PAGE
    // =========================================================

    @GetMapping("/bank/login")
    public String bankLoginPage() {
        return "bank-login";
    }

    // =========================================================
    // BANK ADMIN LOGIN
    // =========================================================

    @PostMapping("/bank/login")
    public String bankLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (username.equals("admin")
                && password.equals("admin123")) {

            session.setAttribute(
                    "bankAdmin",
                    true
            );

            return "redirect:/bank/dashboard";
        }

        model.addAttribute(
                "error",
                "Invalid bank credentials."
        );

        return "bank-login";
    }

    // =========================================================
    // BANK ADMIN DASHBOARD
    // =========================================================

    @GetMapping("/bank/dashboard")
    public String bankDashboard(
            HttpSession session,
            Model model) {

        if (!Boolean.TRUE.equals(
                session.getAttribute("bankAdmin"))) {

            return "redirect:/bank/login";
        }

        model.addAttribute(
                "loans",
                service.allLoans()
        );

        model.addAttribute(
                "userCount",
                service.userCount()
        );

        model.addAttribute(
                "accountCount",
                service.accountCount()
        );

        return "bank-dashboard";
    }

    // =========================================================
    // SANCTION LOAN
    // =========================================================

    @PostMapping("/bank/loan/sanction")
    public String sanction(
            @RequestParam Long id,
            HttpSession session) {

        if (Boolean.TRUE.equals(
                session.getAttribute("bankAdmin"))) {

            service.sanction(id);
        }

        return "redirect:/bank/dashboard";
    }

    // =========================================================
    // REJECT LOAN
    // =========================================================

    @PostMapping("/bank/loan/reject")
    public String reject(
            @RequestParam Long id,
            HttpSession session) {

        if (Boolean.TRUE.equals(
                session.getAttribute("bankAdmin"))) {

            service.reject(id);
        }

        return "redirect:/bank/dashboard";
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @GetMapping("/logout")
    public String logout(
            HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}