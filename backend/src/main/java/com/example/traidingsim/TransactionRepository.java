package com.example.traidingsim;

import com.example.traidingsim.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
