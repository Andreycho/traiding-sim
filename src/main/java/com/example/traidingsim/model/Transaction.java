package com.example.traidingsim.model;

import com.example.traidingsim.Type;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    private String crypto;
    private double amount;
    private double price;
    private double total;
    private String dateTime;

    @Enumerated(EnumType.STRING)
    private Type type;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Transaction() {}

    public Transaction(String crypto, double amount, double price, double total, Type type) {
        this.crypto = crypto;
        this.amount = amount;
        this.price = price;
        this.total = total;
        this.dateTime = LocalDateTime.now().format( FORMATTER);
        this.type = type;
    }

    public long getId() { return Id; }

    public void setId(long Id) { this.Id = Id; }

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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}