package com.expensetracker.imports;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "import_batches")
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    @Column(name = "row_count", nullable = false)
    private int rowCount;

    @Column(name = "imported_count", nullable = false)
    private int importedCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    @Column(name = "reverted_at")
    private Instant revertedAt;

    protected ImportBatch() {
    }

    public ImportBatch(String fileName, String contentHash, Instant importedAt,
                       int rowCount, int importedCount, int skippedCount) {
        this.fileName = fileName;
        this.contentHash = contentHash;
        this.importedAt = importedAt;
        this.rowCount = rowCount;
        this.importedCount = importedCount;
        this.skippedCount = skippedCount;
    }

    public boolean isReverted() {
        return revertedAt != null;
    }

    public void markReverted(Instant when) {
        this.revertedAt = when;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentHash() {
        return contentHash;
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
}
