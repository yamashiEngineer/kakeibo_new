package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import com.example.demo.Service.TransactionService; // TransactionServiceの実際のパッケージ
import com.example.demo.Util.SessionUtil;         // SessionUtilの実際のパッケージ
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;
import com.example.demo.Service.CategoryService; // 欠けていたインポートを追加
import com.example.demo.Entity.Transaction;      // 欠けていたインポートを追加

@Controller
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    public TransactionController(TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    // GET /transactions : 一覧表示
    @GetMapping
    public String list(HttpSession session, Model model) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        model.addAttribute("transactions", transactionService.findAllByUser(userId));
        return "transactions/list";
    }

    // GET /transactions/new : 新規登録フォーム表示
    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        model.addAttribute("categories", categoryService.getCategories(userId));
        return "transactions/form";
    }

    // POST /transactions : 新規登録処理
    @PostMapping
    public String create(@RequestParam String type,
                         @RequestParam Integer amount,
                         @RequestParam Long categoryId,
                         @RequestParam String txnDate,
                         @RequestParam(required = false) String memo,
                         HttpSession session) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setCategoryId(categoryId);
        transaction.setTxnDate(LocalDate.parse(txnDate));
        transaction.setMemo(memo);

        transactionService.save(transaction);
        return "redirect:/transactions";
    }

    // GET /transactions/{id}/edit : 編集フォーム表示
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        try {
            // 自分のデータが存在するか確認して取得
            Transaction transaction = transactionService.findByIdAndUser(id, userId);
            model.addAttribute("transaction", transaction);
            model.addAttribute("categories", categoryService.getCategories(userId));
            return "transactions/form";
        } catch (Exception e) {
            // 対象レコードが存在しない（または他ユーザーのデータ）場合は一覧へリダイレクト
            return "redirect:/transactions";
        }
    }

    // POST /transactions/{id} : 更新処理
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String type,
                         @RequestParam Integer amount,
                         @RequestParam Long categoryId,
                         @RequestParam String txnDate,
                         @RequestParam(required = false) String memo,
                         HttpSession session) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        try {
            // 更新対象のデータを取得し、値を上書きする
            Transaction transaction = transactionService.findByIdAndUser(id, userId);
            transaction.setType(type);
            transaction.setAmount(amount);
            transaction.setCategoryId(categoryId);
            transaction.setTxnDate(LocalDate.parse(txnDate));
            transaction.setMemo(memo);

            transactionService.save(transaction);
        } catch (Exception e) {
            // 対象が見つからない場合はそのまま一覧へ
        }
        return "redirect:/transactions";
    }

    // POST /transactions/{id}/delete : 削除処理
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        transactionService.deleteByIdAndUser(id, userId);
        return "redirect:/transactions";
    }
}