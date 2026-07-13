package com.example.demo.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter // @Data の代わりにこの2つを使う
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ↓ 修正箇所: nameを明示し、Hibernateが混乱しないよう読み取り専用（insert/updateをfalse）に設定
    @Column(name = "user_id", nullable = false, insertable = false, updatable = false, length = 20)
    // 外部キーのためエラーメッセージはログ用
    @NotNull(message = "ユーザー識別子が設定されていません")
    private Long userId;

    @Column(nullable = false)
    @NotNull(message = "金額は必須です")
    // 【追加】1円以上の正の整数であることの制約
    @Min(value = 1, message = "金額は1以上の正の整数を入力してください")
    private Integer amount;

    // ↓ 修正箇所: nameを明示し、Hibernateが混乱しないよう読み取り専用（insert/updateをfalse）に設定
    @Column(name = "category_id", nullable = false, insertable = false, updatable = false, length = 20)
    // 外部キーのためエラーメッセージはログ用
    @NotNull(message = "カテゴリ識別子が設定されていません")
    private Long categoryId;

    @Column(length = 200)
    private String memo;

    @Column(nullable = false)
    private LocalDate txnDate;

    @Column(nullable = false, length = 10)
    private String type; // "income" or "expense"

    // updatable = false で更新時の上書きを防ぐ
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // INSERTされる直前に自動で実行されるメソッド
    @PrePersist
    public void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // User : Transaction = 1 : 多 (こちらが多の側)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Category : Transaction = 1 : 多 (こちらが多の側)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

}
