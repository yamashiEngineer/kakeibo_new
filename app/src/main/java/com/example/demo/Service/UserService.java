package com.example.demo.Service;

import com.example.demo.Entity.Category;
import com.example.demo.Entity.User;
import com.example.demo.Exception.AuthenticationException;
import com.example.demo.Exception.DuplicateEmailException;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if (this.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException("このメールアドレスは既に登録されています");
        }

        // 2. ユーザーの作成とパスワードのハッシュ化
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);

        // 3. 【確実な方法】先にユーザーをデータベースに保存し、IDを確定させる
        User savedUser = userRepository.save(user);

        // 4. デフォルトカテゴリの作成と、保存済みユーザー（親）のセット
        List<String> defaultCategoryNames = List.of("食費", "交通費", "光熱費", "給与", "その他");

        List<Category> defaultCategories = defaultCategoryNames.stream().map(catName -> {
            Category category = new Category();
            category.setName(catName);
            category.setUser(savedUser); // ID確定済みの user をセット
            return category;
        }).collect(Collectors.toList());

        // 5. 【確実な方法】カテゴリを明示的に一括保存する（Cascadeに頼らない）
        categoryRepository.saveAll(defaultCategories);

        return savedUser;
    }

    public User authenticate(String email, String password) {
        // 1. メールアドレスからユーザーを取得（存在しない場合は例外）
        User user = this.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("メールアドレスまたはパスワードが正しくありません"));

        // 2. パスワードの照合（一致しない場合は例外）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("メールアドレスまたはパスワードが正しくありません");
        }

        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}