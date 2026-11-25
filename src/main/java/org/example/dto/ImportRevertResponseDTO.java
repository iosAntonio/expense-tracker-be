package org.example.dto;

public class ImportRevertResponseDTO {

    private ImportBatchDTO batch;
    private int removedCount;

    public ImportRevertResponseDTO() {
    }

    public ImportRevertResponseDTO(ImportBatchDTO batch, int removedCount) {
        this.batch = batch;
        this.removedCount = removedCount;
    }

    public ImportBatchDTO getBatch() {
        return batch;
    }

    public int getRemovedCount() {
        return removedCount;
    }

    public void setBatch(ImportBatchDTO batch) {
        this.batch = batch;
    }

    public void setRemovedCount(int removedCount) {
        this.removedCount = removedCount;
    }
}
