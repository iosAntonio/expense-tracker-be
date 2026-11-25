package com.expensetracker.imports;

import java.util.List;

import com.expensetracker.expense.ExpenseResponse;

public record ImportResult(
        ImportBatchResponse batch,
        List<ExpenseResponse> imported,
        List<String> skippedExternalIds
) {
}
