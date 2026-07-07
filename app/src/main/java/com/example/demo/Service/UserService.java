package com.example.demo.Service;

import com.example.demo.Entity.User;
import com.example.demo.Entity.Category;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.demo.Exception.AuthenticationException;
import com.example.demo.Exception.DuplicateEmailException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // トランザクションを付与して、ユーザー登録とカテゴリ作成をセットで処理する
    @Transactional
    public User register(String email, String password, String name) {

        // 1. メールアドレスの重複チェック
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException("このメールアドレスは既に登録されています");
        }

        // 2. ユーザーの作成とパスワードのハッシュ化
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // パスワードのハッシュ化
        user.setName(name);

        // 3. ユーザーをデータベースに保存 (ここでidが自動採番される)
        User savedUser = userRepository.save(user);

        // 4. 作成するデフォルトカテゴリの名前リスト
        List<String> defaultCategoryNames = List.of(
                "食費", "交通費", "光熱費", "給与", "その他"
        );

        // 5. カテゴリエンティティのリストを作成
        List<Category> defaultCategories = defaultCategoryNames.stream().map(catName -> {
            Category category = new Category();
            category.setName(catName);
            category.setUser(savedUser); // 先ほど保存・採番されたユーザーをセット
            return category;
        }).collect(Collectors.toList());

        // 6. カテゴリを一括でデータベースに保存
        categoryRepository.saveAll(defaultCategories);

        // 7. 登録されたユーザー情報を返す
        return savedUser;
    }

    public User authenticate(String email, String password) {
        // 1. メールアドレスからユーザーを取得（存在しない場合は例外）
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("メールアドレスまたはパスワードが正しくありません"));

        // 2. パスワードの照合（一致しない場合は例外）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("メールアドレスまたはパスワードが正しくありません");
        }

        // 3. 認証に成功したユーザー情報を返す
        return user;
    }

    /**
     * メールアドレスでユーザーを検索する
     * @param email メールアドレス
     * @return ユーザー情報（存在しない場合は空のOptional）
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}