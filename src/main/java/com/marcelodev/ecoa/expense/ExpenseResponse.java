package com.marcelodev.ecoa.expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExpenseResponse(UUID id, BigDecimal amount, String description, Category category, Instant createdAt) {

    static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(expense.getId(), expense.getAmount(), expense.getDescription(), expense.getCategory(),
                expense.getCreatedAt());
    }
}
