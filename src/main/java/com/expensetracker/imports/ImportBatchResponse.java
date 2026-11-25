package com.expensetracker.imports;

import java.time.Instant;

public record ImportBatchResponse(
        Long id,
        String fileName,
        Instant importedAt,
        int rowCount,
        int importedCount,
        int skippedCount,
        Instant revertedAt
) {

    static ImportBatchResponse from(ImportBatch batch) {
        return new ImportBatchResponse(
                batch.getId(),
                batch.getFileName(),
                batch.getImportedAt(),
                batch.getRowCount(),
                batch.getImportedCount(),
                batch.getSkippedCount(),
                batch.getRevertedAt());
    }
}
