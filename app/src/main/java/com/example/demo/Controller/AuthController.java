package com.example.demo.Controller;

// Spring Framework / Servlet API の import
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession; // ※Spring Boot 2.x 以前の場合は javax.servlet.http.HttpSession

// プロジェクト独自のクラスの import (※パッケージ名は実際の構成に合わせて書き換えてください)
import com.example.demo.Service.UserService;
import com.example.demo.Entity.User; // model や domain の場合もあります
import com.example.demo.Util.SessionUtil;
import com.example.demo.Exception.AuthenticationException;
import com.example.demo.Exception.DuplicateEmailException;

@Controller
public class    AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // メソッド名を showLogin に変更、戻り値を auth/login に変更
    @GetMapping("/login")
    public String showLogin() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.authenticate(email, password);
            SessionUtil.setLoginUserId(session, user.getId());
            return "redirect:/transactions"; // ログイン成功時は一覧へ
        } catch (AuthenticationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            // エラー時の戻り値も auth/login に変更
            return "auth/login";
        }
    }

    // メソッド名を showRegister に変更、戻り値を auth/register に変更
    @GetMapping("/register")
    public String showRegister() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String name,
                           HttpSession session,
                           Model model) {
        try {
            User user = userService.register(email, password, name);
            SessionUtil.setLoginUserId(session, user.getId()); // 登録後即時ログイン
            return "redirect:/transactions";
        } catch (DuplicateEmailException e) {
            model.addAttribute("errorMessage", e.getMessage());
            // エラー時の戻り値も auth/register に変更
            return "auth/register";
        }
    }

    // 設計書に合わせて GET ではなく POST メソッドに変更
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        SessionUtil.invalidate(session);
        return "redirect:/login";
    }
}