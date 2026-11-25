package com.expensetracker.imports;

import java.util.List;

public record ImportCheckResult(List<ImportBatchResponse> previousImports) {
}
