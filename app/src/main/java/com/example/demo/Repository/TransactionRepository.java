package com.example.demo.Repository;

import com.example.demo.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByTxnDateDesc(Long userId);
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
    boolean existsByCategoryId(Long categoryId);
}
