package com.example.traidingsim.repository;

import com.example.traidingsim.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
