package org.example.repository;

import org.example.entity.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {

    List<ImportBatch> findAllByOrderByImportedAtDescIdDesc();

    List<ImportBatch> findByContentHashAndRevertedAtIsNullOrderByImportedAtDesc(String contentHash);
}
