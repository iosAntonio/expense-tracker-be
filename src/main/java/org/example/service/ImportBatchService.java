package org.example.service;

import org.example.dto.ExpenseRequestDTO;
import org.example.dto.ImportBatchDTO;
import org.example.dto.ImportCheckResponseDTO;
import org.example.dto.ImportRevertResponseDTO;
import org.example.entity.ImportBatch;
import org.example.repository.ExpenseRepository;
import org.example.repository.ImportBatchRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class ImportBatchService {

    static final String DEFAULT_FILE_NAME = "import.json";
    private static final int MAX_FILE_NAME_LENGTH = 255;

    private final ImportBatchRepository importBatchRepository;
    private final ExpenseRepository expenseRepository;

    public ImportBatchService(
            ImportBatchRepository importBatchRepository,
            ExpenseRepository expenseRepository) {

        this.importBatchRepository = importBatchRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ImportBatch register(
            String fileName,
            List<ExpenseRequestDTO> requests,
            int importedCount,
            int skippedCount) {

        ImportBatch batch = new ImportBatch();
        batch.setFileName(normalizeFileName(fileName));
        batch.setContentHash(ImportFingerprint.of(requests));
        batch.setImportedAt(Instant.now());
        batch.setRowCount(requests.size());
        batch.setImportedCount(importedCount);
        batch.setSkippedCount(skippedCount);
        return importBatchRepository.save(batch);
    }

    @Transactional(readOnly = true)
    public ImportCheckResponseDTO findPreviousImports(List<ExpenseRequestDTO> requests) {
        String contentHash = ImportFingerprint.of(requests);
        List<ImportBatchDTO> previousImports = importBatchRepository
                .findByContentHashAndRevertedAtIsNullOrderByImportedAtDesc(contentHash)
                .stream()
                .map(this::convertToDTO)
                .toList();
        return new ImportCheckResponseDTO(previousImports);
    }

    @Transactional(readOnly = true)
    public List<ImportBatchDTO> getAllImports() {
        return importBatchRepository.findAllByOrderByImportedAtDescIdDesc()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public ImportRevertResponseDTO revert(Long id) {
        ImportBatch batch = importBatchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Import non trovato."
                ));

        if (batch.isReverted()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Questo import è già stato annullato."
            );
        }

        int removedCount = expenseRepository.deleteByImportBatchId(batch.getId());
        batch.setRevertedAt(Instant.now());

        return new ImportRevertResponseDTO(convertToDTO(batch), removedCount);
    }

    public ImportBatchDTO convertToDTO(ImportBatch batch) {
        ImportBatchDTO dto = new ImportBatchDTO();
        dto.setId(batch.getId());
        dto.setFileName(batch.getFileName());
        dto.setImportedAt(batch.getImportedAt());
        dto.setRowCount(batch.getRowCount());
        dto.setImportedCount(batch.getImportedCount());
        dto.setSkippedCount(batch.getSkippedCount());
        dto.setRevertedAt(batch.getRevertedAt());
        return dto;
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return DEFAULT_FILE_NAME;
        }

        String trimmed = fileName.trim();
        return trimmed.length() > MAX_FILE_NAME_LENGTH
                ? trimmed.substring(0, MAX_FILE_NAME_LENGTH)
                : trimmed;
    }
}
