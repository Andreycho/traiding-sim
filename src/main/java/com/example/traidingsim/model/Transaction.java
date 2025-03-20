package com.example.traidingsim.model;

import com.example.traidingsim.Type;

import java.time.LocalDateTime;

public class Transaction {
    private String crypto;
    private double amount;
    private double price;
    private double total;  // Total cost or revenue (price * amount)
    private LocalDateTime dateTime;
    private Type type;  // "BUY" or "SELL"

    public Transaction(String crypto, double amount, double price, double total, Type type) {
        this.crypto = crypto;
        this.amount = amount;
        this.price = price;
        this.total = total;
        this.dateTime = LocalDateTime.now();
        this.type = type;
    }

    public String getCrypto() {
        return crypto;
    }

    public void setCrypto(String crypto) {
        this.crypto = crypto;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}