
package org.example.dto;

import java.util.List;

public class ExpenseImportResponseDTO {

    private int totalReceived;
    private int importedCount;
    private int alreadyImportedCount;
    private List<ExpenseResponseDTO> importedExpenses;
    private List<String> skippedExternalIds = List.of();
    private ImportBatchDTO batch;

    public ExpenseImportResponseDTO() {
    }

    public ExpenseImportResponseDTO(
            int totalReceived,
            int importedCount,
            int alreadyImportedCount,
            List<ExpenseResponseDTO> importedExpenses) {

        this.totalReceived = totalReceived;
        this.importedCount = importedCount;
        this.alreadyImportedCount = alreadyImportedCount;
        this.importedExpenses = importedExpenses;
    }

    public int getTotalReceived() {
        return totalReceived;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public int getAlreadyImportedCount() {
        return alreadyImportedCount;
    }

    public List<ExpenseResponseDTO> getImportedExpenses() {
        return importedExpenses;
    }

    public List<String> getSkippedExternalIds() {
        return skippedExternalIds;
    }

    public ImportBatchDTO getBatch() {
        return batch;
    }

    public void setSkippedExternalIds(List<String> skippedExternalIds) {
        this.skippedExternalIds = skippedExternalIds;
    }

    public void setBatch(ImportBatchDTO batch) {
        this.batch = batch;
    }
}
