package com.expensetracker.imports;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {

    List<ImportBatch> findAllByOrderByImportedAtDescIdDesc();

    List<ImportBatch> findByContentHashAndRevertedAtIsNullOrderByImportedAtDesc(String contentHash);
}
