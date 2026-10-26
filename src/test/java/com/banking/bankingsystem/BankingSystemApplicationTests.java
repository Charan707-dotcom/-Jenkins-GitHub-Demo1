
package com.banking.bankingsystem;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.banking.bankingsystem.service.BankingService;

class BankingSystemApplicationTests {

    private final BankingService service = new BankingService(
            null, null, null, null
    );

    @Test
    void applicationTestRuns() {
        assertTrue(true, "JUnit test passed");
    }

    @Test
    void shouldAllowWithdrawalWithinBalance() {
        assertTrue(service.hasSufficientBalance(1000, 500));
    }

    @Test
    void shouldRejectWithdrawalAboveBalance() {
        assertFalse(service.hasSufficientBalance(1000, 1500));
    }

    @Test
    void shouldRejectZeroWithdrawal() {
        assertFalse(service.hasSufficientBalance(1000, 0));
    }
}
