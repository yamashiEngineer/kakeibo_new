package com.example.demo.Util;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    private static final String LOGIN_USER_ID_KEY = "loginUserId";

    public static Long getLoginUserId(HttpSession session) {
        return (Long) session.getAttribute(LOGIN_USER_ID_KEY);
    }

    public static void setLoginUserId(HttpSession session, Long userId) {
        session.setAttribute(LOGIN_USER_ID_KEY, userId);
    }

    public static void invalidate(HttpSession session) {
        session.invalidate();
    }
}
