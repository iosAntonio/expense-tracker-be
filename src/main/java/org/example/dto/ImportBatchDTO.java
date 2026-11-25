package org.example.dto;

import java.time.Instant;

public class ImportBatchDTO {

    private Long id;
    private String fileName;
    private Instant importedAt;
    private int rowCount;
    private int importedCount;
    private int skippedCount;
    private Instant revertedAt;

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public Instant getImportedAt() {
        return importedAt;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public Instant getRevertedAt() {
        return revertedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setImportedAt(Instant importedAt) {
        this.importedAt = importedAt;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public void setRevertedAt(Instant revertedAt) {
        this.revertedAt = revertedAt;
    }
}
