
package org.example.dto;

import java.util.List;

public class ExpenseImportResponseDTO {

    private int totalReceived;
    private int importedCount;
    private int alreadyImportedCount;
    private List<ExpenseResponseDTO> importedExpenses;

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
}
