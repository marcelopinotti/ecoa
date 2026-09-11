package com.marcelodev.ecoa.expense;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseTools expenseTools;

    public ExpenseController(ExpenseTools expenseTools) {
        this.expenseTools = expenseTools;
    }

    @GetMapping
    public List<ExpenseResponse> list(@RequestParam(required = false) Category category) {
        return expenseTools.list(category);
    }
}
