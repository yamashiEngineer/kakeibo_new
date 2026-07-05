package com.example.demo.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ↓ 修正箇所: nameを明示し、Hibernateが混乱しないよう読み取り専用（insert/updateをfalse）に設定
    @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
    // 外部キーのためエラーメッセージはログ用
    @NotNull(message = "ユーザー識別子が設定されていません")
    private Long userId;

    @Column(nullable = false, length = 50)
    @NotNull(message = "カテゴリ名は必須です")
    private String name;

    // User : Category = 1 : 多 (こちらが多の側)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Category : Transaction = 1 : 多
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

}
