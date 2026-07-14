package com.example.demo.Exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // システム全体で発生した予期せぬエラーをキャッチ
    @ExceptionHandler(Exception.class)
    public String handleSystemError(Exception e, Model model) {
        // ログ出力（開発者が見るため）
        e.printStackTrace();

        return "error/500";
    }
}