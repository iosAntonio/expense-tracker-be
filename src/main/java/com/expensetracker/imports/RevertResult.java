package com.expensetracker.imports;

public record RevertResult(ImportBatchResponse batch, int removedCount) {
}
