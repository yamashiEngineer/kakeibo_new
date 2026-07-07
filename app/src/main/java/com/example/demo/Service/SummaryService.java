package com.example.demo.Service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import java.util.*;
import com.example.demo.Entity.Transaction;
import com.example.demo.Repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final TransactionRepository transactionRepository;

    /**
     * 指定されたユーザーの月次集計データ（収入・支出・収支）を取得する
     * * @param userId ユーザーID
     * @return 月ごとの集計結果を含むマップのリスト
     */
    public List<Map<String, Object>> getMonthlySummary(Long userId) {
        // 1. 対象ユーザーの収支データを日付の降順で取得
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTxnDateDesc(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 2. 年月をキーとして、[0]: 収入合計, [1]: 支出合計 を保持するマップ（順序保持のため LinkedHashMap を使用）
        Map<String, int[]> summaryMap = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            String month = t.getTxnDate().format(formatter);
            summaryMap.putIfAbsent(month, new int[]{0, 0}); // 初期化: [0]=収入, [1]=支出

            if ("income".equals(t.getType())) {
                summaryMap.get(month)[0] += t.getAmount(); // 収入を加算
            } else if ("expense".equals(t.getType())) {
                summaryMap.get(month)[1] += t.getAmount(); // 支出を加算
            }
        }

        // 3. 集計用マップを設計書指定の形式（List<Map<String, Object>>）に変換して返却
        return summaryMap.entrySet().stream().map(entry -> {
            Map<String, Object> map = new HashMap<>();
            int income = entry.getValue()[0];
            int expense = entry.getValue()[1];

            map.put("month", entry.getKey());
            map.put("income", income);
            map.put("expense", expense);
            map.put("balance", income - expense); // 収支（収入 - 支出）を計算
            return map;
        }).collect(Collectors.toList());
    }
}