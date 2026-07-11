package com.example.demo.interceptor;

import com.example.demo.Util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        // セッションが存在しない、またはログイン情報がない場合は弾く
        if (session == null || SessionUtil.getLoginUserId(session) == null) {
            response.sendRedirect("/login");
            return false; // コントローラーの処理を中断
        }
        return true; // アクセス許可
    }
}