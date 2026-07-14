package com.example.demo.Controller;

import com.example.demo.Exception.CategoryInUseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import com.example.demo.Service.CategoryService;
import com.example.demo.Util.SessionUtil;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // GET /categories : カテゴリ一覧表示
    @GetMapping
    public String index(HttpSession session, Model model) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        model.addAttribute("categories", categoryService.getCategories(userId));
        return "categories/index";
    }

    // POST /categories : カテゴリ新規登録
    @PostMapping
    public String create(@RequestParam String name, HttpSession session) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        categoryService.addCategory(userId, name);
        return "redirect:/categories";
    }

    // POST /categories/{id}/delete : カテゴリ削除
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        try {
            categoryService.deleteCategory(id, userId);
        } catch (CategoryInUseException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.getCategories(userId));
            return "categories/index"; // エラー時に一覧画面を再表示
        }
        return "redirect:/categories";
    }
}