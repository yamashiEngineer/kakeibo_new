package com.example.demo.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

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

    // User : Category = 1 : 多
    // mappedByには、Categoryクラス側で定義するフィールド名を指定します
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories;

    // User : Transaction = 1 : 多
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

}
