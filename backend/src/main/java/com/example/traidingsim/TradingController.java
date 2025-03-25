package com.example.traidingsim;

import com.example.traidingsim.model.Transaction;
import com.example.traidingsim.model.dto.ApiResponse;
import com.example.traidingsim.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * Endpoint to fetch real-time cryptocurrency prices.
     * @return A map of cryptocurrency symbols to their current prices.
     */
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
    public ResponseEntity<ApiResponse> buyCrypto(@RequestParam String crypto, @RequestParam double amount) {
        try {
            String message = tradingService.buyCrypto(crypto, amount);
            return ResponseEntity.ok(new ApiResponse(true, message));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
        }
    }
    
    /**
     * Endpoint to sell cryptocurrency.
     * @param crypto The cryptocurrency symbol to sell (e.g., BTC, ETH).
     * @param amount The amount of cryptocurrency to sell.
     * @return Success or error message.
     */
    @PostMapping("/sell")
    public ResponseEntity<ApiResponse> sellCrypto(@RequestParam String crypto, @RequestParam double amount) {
        try {
            String message = tradingService.sellCrypto(crypto, amount);
            return ResponseEntity.ok(new ApiResponse(true, message));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
        }
    }

    /**
     * Endpoint to retrieve the transaction history.
     * @return A list of all transactions made (both buy and sell).
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactionHistory() {
        return ResponseEntity.ok(tradingService.getTransactionHistory());
    }


    /**
     * Endpoint to reset the account balance and holdings.
     * @return Success message indicating the account has been reset.
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse> resetAccount() {
        try {
            String message = tradingService.resetAccount();
            return ResponseEntity.ok(new ApiResponse(true, message));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
        }
    }

    /**
     * Endpoint to fetch the current account balance.
     * @return The current account balance as a double.
     */
    @GetMapping("/balance")
    public ResponseEntity<Double> getAccountBalance() {
        return ResponseEntity.ok(tradingService.getAccountBalance());
    }


    /**
     * Endpoint to fetch current cryptocurrency holdings.
     * @return A map of cryptocurrencies and the amounts held.
     */
    @GetMapping("/holdings")
    public ResponseEntity<Map<String, Double>> getCryptoHoldings() {
        return ResponseEntity.ok(tradingService.getCryptoHoldings());
    }

    /**
     * Endpoint to fetch the profit or loss made from all transactions.
     * @return A map of the profit or loss made from all transactions.
     */
    @GetMapping("/profit-loss")
    public ResponseEntity<Map<String, Double>> getProfitLoss() {
        Map<String, Double> profitLossMap = tradingService.calculateProfitLoss();
        return ResponseEntity.ok(profitLossMap);
    }

}