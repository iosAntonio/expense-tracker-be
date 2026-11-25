package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

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

    public ImportBatch() {
    }

    public boolean isReverted() {
        return revertedAt != null;
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
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
