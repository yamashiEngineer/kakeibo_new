package com.example.demo.Service;

import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.example.demo.Entity.Transaction;
import com.example.demo.Repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * ユーザーの収支一覧を取得
     */
    public List<Transaction> findAllByUser(Long userId) {
        // ※リポジトリのメソッド名は既存の実装（日付降順）を維持しています
        return transactionRepository.findByUserIdOrderByTxnDateDesc(userId);
    }

    /**
     * 指定したIDとユーザーIDの収支を取得
     */
    public Transaction findByIdAndUser(Long id, Long userId) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("データが見つかりません"));
    }

    /**
     * 収支を新規・更新共通
     */
    @Transactional
    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    /**
     * 収支を削除
     */
    @Transactional
    public void deleteByIdAndUser(Long id, Long userId) {
        // findByIdAndUserIdメソッドを再利用して、存在チェックと他ユーザーのデータでないかを確認
        Transaction transaction = findByIdAndUser(id, userId);
        transactionRepository.delete(transaction);
    }

    /**
     * 収支リストから収入合計を計算する
     * @param list 収支データのリスト
     * @return 収入の合計金額
     */
    public int calcTotalIncome(List<Transaction> list) {
        if (list == null || list.isEmpty()) return 0;

        return list.stream()
                .filter(t -> "income".equals(t.getType()))
                .mapToInt(Transaction::getAmount)
                .sum();
    }

    /**
     * 収支リストから支出合計を計算する
     * @param list 収支データのリスト
     * @return 支出の合計金額
     */
    public int calcTotalExpense(List<Transaction> list) {
        if (list == null || list.isEmpty()) return 0;

        return list.stream()
                .filter(t -> "expense".equals(t.getType()))
                .mapToInt(Transaction::getAmount)
                .sum();
    }
}