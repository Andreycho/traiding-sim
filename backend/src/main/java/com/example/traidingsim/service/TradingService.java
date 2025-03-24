package com.example.traidingsim.service;

import com.example.traidingsim.exception.CryptoNotFoundException;
import com.example.traidingsim.exception.InsufficientFundsException;
import com.example.traidingsim.exception.InsufficientHoldingsException;
import com.example.traidingsim.model.enumeration.Type;
import com.example.traidingsim.repository.AccountRepository;
import com.example.traidingsim.repository.TransactionRepository;
import com.example.traidingsim.model.Account;
import com.example.traidingsim.model.Transaction;

import com.example.traidingsim.websocket.KrakenWebSocketService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.traidingsim.model.enumeration.Type.BUY;
import static com.example.traidingsim.model.enumeration.Type.SELL;

@Service
@Slf4j
public class TradingService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private static final double INITIAL_BALANCE = 10000.0;
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
    private final Map<String, Double> cryptoHoldings;

    private final KrakenWebSocketService krakenWebSocketService;

    @Autowired
    public TradingService(KrakenWebSocketService krakenWebSocketService, AccountRepository accountRepository) {
        this.krakenWebSocketService = krakenWebSocketService;
        this.accountRepository = accountRepository;
        Account account = accountRepository.findById(1L).orElseGet(() -> {
            Account newAccount = new Account(INITIAL_BALANCE);
            accountRepository.save(newAccount);
            return newAccount;
        });

        this.accountBalance = account.getBalance();
        this.cryptoHoldings = account.getCryptoHoldings();
    }

    /**
     * Fetch real-time crypto prices from KrakenWebSocketService.
     */
    public Map<String, Double> getCryptoPrices() {
        return krakenWebSocketService.getCryptoPrices();
    }

    /**
     * Retrieve the transaction history.
     * @return A list of all transactions made (both buy and sell).
     */
    public List<Transaction> getTransactionHistory() { return transactionRepository.findAll(); }

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
            throw new CryptoNotFoundException("Cryptocurrency '" + crypto + "' not available.");
        }

        double price = prices.get(key);
        double totalCost = price * amount;

        if (totalCost > accountBalance) {
            throw new InsufficientFundsException("Insufficient funds. Your balance is $" + accountBalance);
        }

        accountBalance -= totalCost;
        cryptoHoldings.put(crypto, cryptoHoldings.getOrDefault(crypto, 0.0) + amount);

        Account account = accountRepository.findById(1L).orElse(new Account(INITIAL_BALANCE));
        account.setBalance(accountBalance);
        account.setCryptoHoldings(cryptoHoldings);
        accountRepository.save(account);

        Transaction transaction = new Transaction(crypto, amount, price, totalCost, BUY);

        transactionRepository.save(transaction);

        log.info("Successfully bought {} {} for ${}", amount, crypto, totalCost);

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
            throw new InsufficientHoldingsException("Insufficient holdings of " + crypto);
        }

        Map<String, Double> prices = getCryptoPrices();
        String keyUSD = crypto + "/USD";
        String key = prices.containsKey(crypto) ? crypto : keyUSD;

        if (!prices.containsKey(key)) {
            throw new CryptoNotFoundException("Cryptocurrency '" + crypto + "' not available.");
        }

        double price = prices.get(key);
        double totalRevenue = price * amount;

        double newAmount = cryptoHoldings.get(crypto) - amount;

        if (newAmount <= 0) {
            cryptoHoldings.remove(crypto);
        } else {
            cryptoHoldings.put(crypto, newAmount);
        }

        accountBalance += totalRevenue;

        Account account = accountRepository.findById(1L).orElse(new Account(INITIAL_BALANCE));
        account.setBalance(accountBalance);
        account.setCryptoHoldings(cryptoHoldings);
        accountRepository.save(account);

        Transaction transaction = new Transaction(crypto, amount, price, totalRevenue, SELL);
        transactionRepository.save(transaction);

        log.info("Successfully sold {} {} for ${}", amount, crypto, totalRevenue);

        return "Successfully sold " + amount + " " + crypto + " for $" + totalRevenue;
    }

    public Map<String, Double> calculateProfitLoss() {
        Map<String, Double> profitLossMap = new HashMap<>();
        List<Transaction> transactions = transactionRepository.findAll();

        for (String crypto : getCryptoPrices().keySet()) {
            double totalBought = 0.0;
            double totalSold = 0.0;
            double totalAmountBought = 0.0;
            double totalAmountSold = 0.0;

            for (Transaction transaction : transactions) {
                if (transaction.getCrypto().equals(crypto)) {
                    if (transaction.getType() == Type.BUY) {
                        totalBought += transaction.getTotal();
                        totalAmountBought += transaction.getAmount();
                    } else if (transaction.getType() == Type.SELL) {
                        totalSold += transaction.getTotal();
                        totalAmountSold += transaction.getAmount();
                    }
                }
            }

            if (totalAmountBought > 0) {
                double totalRevenue = totalSold;

                double profitLoss = totalRevenue - totalBought;
                profitLossMap.put(crypto, profitLoss);
            }
        }

        return profitLossMap;
    }




    /**
     * Reset the account balance and clear transaction history.
     */
    public String resetAccount() {
        this.accountBalance = INITIAL_BALANCE;
        this.cryptoHoldings.clear();

        Account account = accountRepository.findById(1L).orElse(new Account(INITIAL_BALANCE));
        account.setBalance(accountBalance);
        account.setCryptoHoldings(cryptoHoldings);
        accountRepository.save(account);

        transactionRepository.deleteAll();

        log.info("Account has been reset to the initial balance of ${}", INITIAL_BALANCE);

        return "Account has been reset to the initial balance of $" + INITIAL_BALANCE;
    }

}