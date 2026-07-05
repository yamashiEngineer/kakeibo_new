package com.example.demo.Repository;

import com.example.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * メールアドレスを条件にユーザーを検索するメソッド
     * ※ログイン処理や、新規登録時の「すでに登録されているメールアドレスか」
     * のチェックなどで必ず必要になります。
     *
     * @param email 検索対象のメールアドレス
     * @return ユーザーが存在する場合はそのUser情報、存在しない場合は空(empty)
     */
    Optional<User> findByEmail(String email);

    // ※もしログインIDで管理している場合は、以下のように記述します
    // Optional<User> findByUsername(String username);
}