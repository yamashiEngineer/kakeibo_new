package com.example.demo.Repository;

import com.example.demo.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 特定のユーザーに紐づくカテゴリ一覧を自動検索するメソッド
     * * Spring Data JPAの機能により、メソッド名（findByUserId）から
     * 「WHERE user_id = ?」というSQLが自動的に生成されます。
     *
     * @param userId ユーザーID
     * @return 該当ユーザーのカテゴリリスト
     */
    List<Category> findByUserId(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

}