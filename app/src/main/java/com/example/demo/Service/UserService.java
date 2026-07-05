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


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // トランザクションを付与して、ユーザー登録とカテゴリ作成をセットで処理する
    @Transactional
    public void registerNewUser(User user) {

        // 1. ユーザーをデータベースに保存 (ここでidが自動採番される)
        User savedUser = userRepository.save(user);

        // 2. 作成するデフォルトカテゴリの名前リスト (Java 17以降の List.of を活用)
        List<String> defaultCategoryNames = List.of(
                "食費", "交通費", "光熱費", "給与", "その他"
        );

        // 3. カテゴリエンティティのリストを作成
        List<Category> defaultCategories = defaultCategoryNames.stream().map(name -> {
            Category category = new Category();
            category.setName(name);
            category.setUser(savedUser); // 先ほど保存・採番されたユーザーをセット
            return category;
        }).collect(Collectors.toList());

        // 4. カテゴリを一括でデータベースに保存
        categoryRepository.saveAll(defaultCategories);
    }
}