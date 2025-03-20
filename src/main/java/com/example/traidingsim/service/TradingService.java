package com.example.traidingsim.service;

import com.example.traidingsim.model.Transaction;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.traidingsim.Type.BUY;
import static com.example.traidingsim.Type.SELL;

@Service
public class TradingService {

    private static final double INITIAL_BALANCE = 100000.0;
    /**
     * -- GETTER --
     *  Get the current account balance.
     */
    @Getter
    private double accountBalance;
    /**
     * -- GETTER --
     *  Get the holdings for all cryptocurrencies.
     */
    @Getter
    private final Map<String, Double> cryptoHoldings; // Stores holdings by cryptocurrency
    /**
     * -- GETTER --
     *  Retrieve the transaction history.
     */
    @Getter
    private final List<Transaction> transactionHistory;
    private final KrakenWebSocketService krakenWebSocketService;

    @Autowired
    public TradingService(KrakenWebSocketService krakenWebSocketService) {
        this.krakenWebSocketService = krakenWebSocketService;
        this.accountBalance = INITIAL_BALANCE;
        this.cryptoHoldings = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
    }

    /**
     * Fetch real-time crypto prices from KrakenWebSocketService.
     */
    public Map<String, Double> getCryptoPrices() {
        return krakenWebSocketService.getCryptoPrices();
    }

    /**
     * Simulate buying cryptocurrency.
     * @param crypto The cryptocurrency to buy.
     * @param amount The amount to buy (in the cryptocurrency's unit, e.g., BTC or ETH).
     * @return Success or error message.
     */
    public String buyCrypto(String crypto, double amount) {
        Map<String, Double> prices = getCryptoPrices();

        String keyUSD = crypto + "/USD";
        String key = prices.containsKey(crypto) ? crypto : keyUSD;

        if (!prices.containsKey(key)) {
            return "Error: Cryptocurrency not available.";
        }

        double price = prices.get(key);
        double totalCost = price * amount;

        if (totalCost > accountBalance) {
            return "Error: Insufficient funds. Your balance is $" + accountBalance;
        }

        accountBalance -= totalCost;
        cryptoHoldings.put(crypto, cryptoHoldings.getOrDefault(crypto, 0.0) + amount);

        Transaction transaction = new Transaction(crypto, amount, price, totalCost, BUY);
        transactionHistory.add(transaction);

        return "Successfully bought " + amount + " " + crypto + " for $" + totalCost;
    }

    /**
     * Simulate selling cryptocurrency.
     * @param crypto The cryptocurrency to sell.
     * @param amount The amount to sell (in the cryptocurrency's unit, e.g., BTC or ETH).
     * @return Success or error message.
     */
    public String sellCrypto(String crypto, double amount) {
        if (!cryptoHoldings.containsKey(crypto) || cryptoHoldings.getOrDefault(crypto, 0.0) < amount) {
            return "Error: Insufficient holdings of " + crypto;
        }

        Map<String, Double> prices = getCryptoPrices();
        String keyUSD = crypto + "/USD";
        String key = prices.containsKey(crypto) ? crypto : keyUSD;

        if (!prices.containsKey(key)) {
            return "Error: Cryptocurrency not available.";
        }

        double price = prices.get(key);
        double totalRevenue = price * amount;

        cryptoHoldings.put(crypto, cryptoHoldings.get(crypto) - amount);
        accountBalance += totalRevenue;

        Transaction transaction = new Transaction(crypto, amount, price, totalRevenue, SELL);
        transactionHistory.add(transaction);

        return "Successfully sold " + amount + " " + crypto + " for $" + totalRevenue;
    }

    /**
     * Reset the account balance and clear transaction history.
     */
    public String resetAccount() {
        this.accountBalance = INITIAL_BALANCE;
        this.cryptoHoldings.clear();
        this.transactionHistory.clear();
        return "Account has been reset to the initial balance of $" + INITIAL_BALANCE;
    }

}