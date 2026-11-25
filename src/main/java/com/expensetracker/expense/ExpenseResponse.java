package com.expensetracker.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        LocalDate date,
        String description,
        BigDecimal amount,
        Category category,
        String paymentMethod
) {

    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDate(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getPaymentMethod());
    }
}
