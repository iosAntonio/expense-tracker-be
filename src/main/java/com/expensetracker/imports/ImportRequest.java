package com.expensetracker.imports;

import java.util.List;

import com.expensetracker.expense.ExpenseImportRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ImportRequest(
        @NotBlank(message = "il nome del file è obbligatorio") @Size(max = 255) String fileName,
        @NotEmpty(message = "nessuna spesa da importare") List<@Valid ExpenseImportRequest> expenses
) {
}
