package com.example.traidingsim.service;

import com.example.traidingsim.exception.*;
import com.example.traidingsim.model.enumeration.Type;
import com.example.traidingsim.repository.AccountRepository;
import com.example.traidingsim.repository.TransactionRepository;
import com.example.traidingsim.model.Account;
import com.example.traidingsim.model.Transaction;
import com.example.traidingsim.websocket.KrakenWebSocketService;
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

    private final KrakenWebSocketService krakenWebSocketService;

    @Autowired
    public TradingService(KrakenWebSocketService krakenWebSocketService, AccountRepository accountRepository) {
        this.krakenWebSocketService = krakenWebSocketService;
        this.accountRepository = accountRepository;

        accountRepository.findById(1L).orElseGet(() -> {
            Account newAccount = new Account(INITIAL_BALANCE);
            accountRepository.save(newAccount);
            return newAccount;
        });
    }

    /**
     * Fetch real-time crypto prices from KrakenWebSocketService.
     */
    public Map<String, Double> getCryptoPrices() {
        return krakenWebSocketService.getCryptoPrices();
    }

    /**
     * Retrieve the transaction history.
     */
    public List<Transaction> getTransactionHistory() {
        return transactionRepository.findAll();
    }

    /**
     * Retrieve the account balance.
     */
    public double getAccountBalance() {
        Account account = accountRepository.findById(1L).orElseThrow(() ->
                new AccountNotFoundException("Account not found")
        );
        return account.getBalance();
    }

    /**
     * Retrieve the crypto holdings.
     */
    public Map<String, Double> getCryptoHoldings() {
        Account account = accountRepository.findById(1L).orElseThrow(() ->
                new AccountNotFoundException("Account not found")
        );
        return account.getCryptoHoldings();
    }

    /**
     * Buy cryptocurrency.
     */
    public String buyCrypto(String crypto, double amount) {
        validateAmount(amount);

        Map<String, Double> prices = getCryptoPrices();
        String key = getCryptoKey(crypto, prices);

        double price = prices.get(key);
        double totalCost = calculateTotalCost(amount, price);

        Account account = getAccount();

        validateFunds(account, totalCost);

        updateAccountBalance(account, totalCost);
        updateCryptoHoldingsOnBuy(account, crypto, amount);

        saveTransaction(crypto, amount, price, totalCost, BUY);

        log.info("Successfully bought {} {} for ${}", amount, crypto, totalCost);
        return "Successfully bought " + amount + " " + crypto + " for $" + totalCost;
    }

    /**
     * Sell cryptocurrency.
     */
    public String sellCrypto(String crypto, double amount) {
        validateAmount(amount);

        Account account = getAccount();

        validateHoldings(account, crypto, amount);

        Map<String, Double> prices = getCryptoPrices();
        String key = getCryptoKey(crypto, prices);

        double price = prices.get(key);
        double totalRevenue = calculateTotalRevenue(amount, price);

        updateAccountBalance(account, -totalRevenue);
        updateCryptoHoldingsOnSell(account, crypto, amount);

        saveTransaction(crypto, amount, price, totalRevenue, SELL);

        log.info("Successfully sold {} {} for ${}", amount, crypto, totalRevenue);
        return "Successfully sold " + amount + " " + crypto + " for $" + totalRevenue;
    }

    /**
     * Calculate profit/loss.
     */
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
        Account account = accountRepository.findById(1L).orElseGet(() -> new Account(INITIAL_BALANCE));

        account.setBalance(INITIAL_BALANCE);

        account.setCryptoHoldings(new HashMap<>());

        accountRepository.save(account);
        transactionRepository.deleteAll();

        log.info("Account has been reset to the initial balance of ${}", INITIAL_BALANCE);
        return "Account has been reset to the initial balance of $" + INITIAL_BALANCE;
    }

    /**
     * Helper methods.
     */

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than 0");
        }
    }

    private String getCryptoKey(String crypto, Map<String, Double> prices) {
        String keyUSD = crypto + "/USD";
        return prices.containsKey(crypto) ? crypto : keyUSD;
    }

    private double calculateTotalCost(double amount, double price) {
        return price * amount;
    }

    private Account getAccount() {
        return accountRepository.findById(1L).orElseThrow(() ->
                new AccountNotFoundException("Account not found")
        );
    }

    private void validateFunds(Account account, double totalCost) {
        if (totalCost > account.getBalance()) {
            throw new InsufficientFundsException("Insufficient funds. Your balance is $" + account.getBalance());
        }
    }

    private void updateAccountBalance(Account account, double amount) {
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
    }

    private void updateCryptoHoldingsOnBuy(Account account, String crypto, double amount) {
        Map<String, Double> updatedHoldings = new HashMap<>(account.getCryptoHoldings());
        updatedHoldings.put(crypto, updatedHoldings.getOrDefault(crypto, 0.0) + amount);
        account.setCryptoHoldings(updatedHoldings);
        accountRepository.save(account);
    }

    private void updateCryptoHoldingsOnSell(Account account, String crypto, double amount) {
        Map<String, Double> updatedHoldings = new HashMap<>(account.getCryptoHoldings());

        double currentAmount = updatedHoldings.getOrDefault(crypto, 0.0);
        double newAmount = currentAmount - amount;

        if (newAmount <= 0) {
            updatedHoldings.remove(crypto);
        } else {
            updatedHoldings.put(crypto, newAmount);
        }

        account.setCryptoHoldings(updatedHoldings);
        accountRepository.save(account);
    }

    private void validateHoldings(Account account, String crypto, double amount) {
        if (!account.getCryptoHoldings().containsKey(crypto) ||
                account.getCryptoHoldings().getOrDefault(crypto, 0.0) < amount) {
            throw new InsufficientHoldingsException("Insufficient holdings of " + crypto);
        }
    }

    private double calculateTotalRevenue(double amount, double price) {
        return price * amount;
    }

    private void saveTransaction(String crypto, double amount, double price, double totalCost, Type type) {
        Transaction transaction = new Transaction(crypto, amount, price, totalCost, type);
        transaction.setAccount(getAccount());
        transactionRepository.save(transaction);
    }
}
