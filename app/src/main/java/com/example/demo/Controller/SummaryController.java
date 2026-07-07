// src/main/java/com/example/kakeibo/controller/SummaryController.java
package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class SummaryController {
    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/summary")
    public String index(HttpSession session, Model model) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) return "redirect:/login";

        model.addAttribute("summaries", summaryService.getMonthlySummary(userId));
        return "summary/index";
    }
}