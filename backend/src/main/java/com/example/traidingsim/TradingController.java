package com.example.traidingsim;

import com.example.traidingsim.model.Transaction;
import com.example.traidingsim.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow cross-origin requests for frontend integration
public class TradingController {

    private final TradingService tradingService;

    // Constructor-based dependency injection
    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * Endpoint to fetch real-time cryptocurrency prices.
     * @return A map of cryptocurrency symbols to their current prices.
     */
//    @GetMapping("/prices")
//    public Map<String, Double> getRealTimePrices() {
//        return tradingService.getCryptoPrices();
//    }

    @GetMapping("/prices")
    public ResponseEntity<Map<String, Double>> getPrices() {
        Map<String, Double> latestPrices = tradingService.getCryptoPrices();
        return ResponseEntity.ok(latestPrices);
    }

    /**
     * Endpoint to buy cryptocurrency.
     * @param crypto The cryptocurrency symbol to buy (e.g., BTC, ETH).
     * @param amount The amount of cryptocurrency to purchase.
     * @return Success or error message.
     */
    @PostMapping("/buy")
    public String buyCrypto(@RequestParam String crypto, @RequestParam double amount) {
        return tradingService.buyCrypto(crypto, amount);
    }

    /**
     * Endpoint to sell cryptocurrency.
     * @param crypto The cryptocurrency symbol to sell (e.g., BTC, ETH).
     * @param amount The amount of cryptocurrency to sell.
     * @return Success or error message.
     */
    @PostMapping("/sell")
    public String sellCrypto(@RequestParam String crypto, @RequestParam double amount) {
        return tradingService.sellCrypto(crypto, amount);
    }

    /**
     * Endpoint to retrieve the transaction history.
     * @return A list of all transactions made (both buy and sell).
     */
    @GetMapping("/transactions")
    public List<Transaction> getTransactionHistory() {
        return tradingService.getTransactionHistory();
    }

    /**
     * Endpoint to reset the account balance and holdings.
     * @return Success message indicating the account has been reset.
     */
    @PostMapping("/reset")
    public String resetAccount() {
        return tradingService.resetAccount();
    }

    /**
     * Endpoint to fetch the current account balance.
     * @return The current account balance as a double.
     */
    @GetMapping("/balance")
    public double getAccountBalance() {
        return tradingService.getAccountBalance();
    }

    /**
     * Endpoint to fetch current cryptocurrency holdings.
     * @return A map of cryptocurrencies and the amounts held.
     */
    @GetMapping("/holdings")
    public Map<String, Double> getCryptoHoldings() {
        return tradingService.getCryptoHoldings();
    }
}