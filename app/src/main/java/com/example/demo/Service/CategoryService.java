package com.example.demo.Service;

import com.example.demo.Entity.Category;
import com.example.demo.Entity.User;
import com.example.demo.Exception.CategoryInUseException;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Repository.TransactionRepository;
import com.example.demo.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public List<Category> getCategories(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    public void addCategory(Long userId, String name) {
        // 1. ユーザーIDを使って、DBから「Userの実体」を取得する
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

        Category category = new Category();
//        category.setUserId(userId);
        category.setName(name);
        category.setUser(user);
        categoryRepository.save(category);
    }

    public void deleteCategory(Long categoryId, Long userId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("カテゴリが見つかりません"));

        // 例外処理設計: 対象カテゴリが収支データに紐づいているか確認
        if (transactionRepository.existsByCategoryId(categoryId)) {
            throw new CategoryInUseException("このカテゴリは収支データに使用されているため削除できません");
        }
        categoryRepository.delete(category);
    }
}