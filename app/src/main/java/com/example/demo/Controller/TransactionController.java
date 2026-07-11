package com.example.demo.Controller;

import com.example.demo.Entity.Transaction;      // 欠けていたインポートを追加
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.CategoryService; // 欠けていたインポートを追加
import com.example.demo.Service.TransactionService; // TransactionServiceの実際のパッケージ
import com.example.demo.Util.SessionUtil;         // SessionUtilの実際のパッケージ
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService, CategoryService categoryService,
                                 UserRepository userRepository) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    // GET /transactions : 一覧表示
    @GetMapping
    public String list(HttpSession session, Model model) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        // 【追加】セッションのIDを使って、DBから最新のユーザー情報を取得する
        // ※userServiceにfindByIdなどのメソッドがあると仮定しています
        User user = userRepository.findById(userId).orElse(null);

        // 【追加】HTML側で使えるように "loginUser" という名前でModelに詰める
        model.addAttribute("loginUser", user);

        // 2. 取引履歴の一覧を取得してモデルに追加
        List<Transaction> transactions = transactionService.findAllByUser(userId);
        model.addAttribute("transactions", transactions);

        // 3. 【安全対策】データが空（またはnull）でもエラーにならないよう、Java側で合計を計算
        int incomeTotal = 0;
        int expenseTotal = 0;

        if (transactions != null) {
            incomeTotal = transactions.stream()
                    .filter(t -> "income".equals(t.getType()))
                    .mapToInt(t -> t.getAmount()) // ※Getterがない場合は t.amount
                    .sum();

            expenseTotal = transactions.stream()
                    .filter(t -> "expense".equals(t.getType()))
                    .mapToInt(t -> t.getAmount()) // ※Getterがない場合は t.amount
                    .sum();
        }

        // 4. 計算済みの数値をモデルに追加
        model.addAttribute("incomeTotal", incomeTotal);
        model.addAttribute("expenseTotal", expenseTotal);

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