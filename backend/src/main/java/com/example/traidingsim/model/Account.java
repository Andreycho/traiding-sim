package com.example.traidingsim.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private double balance;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "crypto_holdings", joinColumns = @JoinColumn(name = "account_id"))
    @MapKeyColumn(name = "crypto")
    @Column(name = "amount")
    private Map<String, Double> cryptoHoldings = new HashMap<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    public Account() {}

    public Account(double balance) {
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public double getBalance() {
        return balance;
    }

    public Map<String, Double> getCryptoHoldings() {
        return cryptoHoldings;
    }

}
