package com.expensetracker.imports;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expensetracker.config.ApiException;
import com.expensetracker.expense.Expense;
import com.expensetracker.expense.ExpenseImportRequest;
import com.expensetracker.expense.ExpenseRepository;
import com.expensetracker.expense.ExpenseResponse;

@Service
public class ImportService {

    private final ExpenseRepository expenses;
    private final ImportBatchRepository batches;

    public ImportService(ExpenseRepository expenses, ImportBatchRepository batches) {
        this.expenses = expenses;
        this.batches = batches;
    }

    @Transactional
    public ImportResult importExpenses(ImportRequest request) {
        List<ExpenseImportRequest> rows = request.expenses();
        Map<String, ExpenseImportRequest> byExternalId = new LinkedHashMap<>();
        Set<String> skipped = new LinkedHashSet<>();
        for (ExpenseImportRequest row : rows) {
            String externalId = row.externalId().trim();
            if (byExternalId.putIfAbsent(externalId, row) != null) {
                skipped.add(externalId);
            }
        }

        Set<String> existing = expenses.findExistingExternalIds(byExternalId.keySet());
        Map<String, ExpenseImportRequest> toImport = new LinkedHashMap<>();
        byExternalId.forEach((externalId, row) -> {
            if (existing.contains(externalId)) {
                skipped.add(externalId);
            } else {
                toImport.put(externalId, row);
            }
        });

        if (toImport.isEmpty()) {
            return new ImportResult(null, List.of(), List.copyOf(skipped));
        }

        ImportBatch batch = batches.save(new ImportBatch(
                request.fileName().trim(),
                ImportFingerprint.of(rows),
                Instant.now(),
                rows.size(),
                toImport.size(),
                rows.size() - toImport.size()));

        List<Expense> saved = expenses.saveAll(toImport.entrySet().stream()
                .map(entry -> toEntity(entry.getKey(), entry.getValue(), batch.getId()))
                .toList());

        return new ImportResult(
                ImportBatchResponse.from(batch),
                saved.stream().map(ExpenseResponse::from).toList(),
                List.copyOf(skipped));
    }

    @Transactional(readOnly = true)
    public ImportCheckResult check(ImportRequest request) {
        String contentHash = ImportFingerprint.of(request.expenses());
        return new ImportCheckResult(
                batches.findByContentHashAndRevertedAtIsNullOrderByImportedAtDesc(contentHash).stream()
                        .map(ImportBatchResponse::from)
                        .toList());
    }

    @Transactional(readOnly = true)
    public List<ImportBatchResponse> findAll() {
        return batches.findAllByOrderByImportedAtDescIdDesc().stream()
                .map(ImportBatchResponse::from)
                .toList();
    }

    @Transactional
    public RevertResult revert(long id) {
        ImportBatch batch = batches.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Import non trovato."));
        if (batch.isReverted()) {
            throw new ApiException(HttpStatus.CONFLICT, "Questo import è già stato annullato.");
        }

        int removed = expenses.deleteByImportBatchId(batch.getId());
        batch.markReverted(Instant.now());

        return new RevertResult(ImportBatchResponse.from(batch), removed);
    }

    private static Expense toEntity(String externalId, ExpenseImportRequest row, Long batchId) {
        return new Expense(
                externalId,
                row.date(),
                row.description(),
                row.amount(),
                row.category(),
                row.paymentMethod(),
                batchId);
    }
}
