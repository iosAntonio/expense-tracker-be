package org.example.service;

import org.example.dto.ExpenseRequestDTO;
import org.example.dto.ExpenseResponseDTO;
import org.example.dto.ExpenseImportResponseDTO;
import org.example.dto.ImportBatchDTO;
import org.example.dto.MonthDeletionResponseDTO;
import org.example.entity.Expense;
import org.example.entity.ImportBatch;
import org.example.repository.ExpenseRepository;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ImportBatchService importBatchService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ImportBatchService importBatchService) {

        this.expenseRepository = expenseRepository;
        this.importBatchService = importBatchService;
    }

    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO request) {

        Expense expense = convertToEntity(request);

        Expense savedExpense = expenseRepository.save(expense);

        return convertToResponseDTO(savedExpense);
    }

    @Transactional
    public ExpenseImportResponseDTO importExpenses(
            String fileName,
            List<ExpenseRequestDTO> requests) {

        Set<String> externalIds = new HashSet<>();

        for (ExpenseRequestDTO request : requests) {

            String externalId = request.getExternalId();

            if (externalId == null || externalId.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ogni spesa deve avere un externalId"
                );
            }

            if (!externalIds.add(externalId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Il JSON contiene externalId duplicati: " + externalId
                );
            }
        }

        List<String> ids = requests.stream()
                .map(ExpenseRequestDTO::getExternalId)
                .toList();

        Set<String> existingIds = expenseRepository
                .findAllByExternalIdIn(ids)
                .stream()
                .map(Expense::getExternalId)
                .collect(Collectors.toSet());

        List<ExpenseRequestDTO> newRequests = requests.stream()
                .filter(request -> !existingIds.contains(request.getExternalId()))
                .toList();

        List<String> skippedExternalIds = ids.stream()
                .filter(existingIds::contains)
                .toList();

        List<Expense> savedExpenses = List.of();
        ImportBatchDTO batch = null;

        if (!newRequests.isEmpty()) {
            ImportBatch importBatch = importBatchService.register(
                    fileName,
                    requests,
                    newRequests.size(),
                    skippedExternalIds.size()
            );

            List<Expense> newExpenses = newRequests.stream()
                    .map(this::convertToEntity)
                    .toList();
            newExpenses.forEach(expense -> expense.setImportBatchId(importBatch.getId()));

            savedExpenses = expenseRepository.saveAll(newExpenses);
            batch = importBatchService.convertToDTO(importBatch);
        }

        List<ExpenseResponseDTO> importedExpenses = savedExpenses.stream()
                .map(this::convertToResponseDTO)
                .toList();

        ExpenseImportResponseDTO response = new ExpenseImportResponseDTO(
                requests.size(),
                savedExpenses.size(),
                existingIds.size(),
                importedExpenses
        );
        response.setSkippedExternalIds(skippedExternalIds);
        response.setBatch(batch);

        return response;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getAllExpenses() {

        return expenseRepository.findAll(Sort.by(Sort.Direction.DESC, "date", "id"))
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional
    public ExpenseResponseDTO updateExpense(Long id, ExpenseRequestDTO request) {

        Expense expense = findExpense(id);

        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setPaymentMethod(request.getPaymentMethod());

        return convertToResponseDTO(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {

        expenseRepository.delete(findExpense(id));
    }

    @Transactional
    public MonthDeletionResponseDTO deleteMonth(YearMonth month) {

        int deletedCount = expenseRepository.deleteAllInPeriod(
                month.atDay(1),
                month.atEndOfMonth()
        );

        return new MonthDeletionResponseDTO(month.toString(), deletedCount);
    }

    private Expense findExpense(Long id) {

        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Spesa non trovata."
                ));
    }

    private ExpenseResponseDTO convertToResponseDTO(Expense expense) {

        ExpenseResponseDTO response = new ExpenseResponseDTO();

        response.setId(expense.getId());
        response.setDate(expense.getDate());
        response.setDescription(expense.getDescription());
        response.setAmount(expense.getAmount());
        response.setCategory(expense.getCategory());
        response.setPaymentMethod(expense.getPaymentMethod());

        return response;
    }

    private Expense convertToEntity(ExpenseRequestDTO request) {

        Expense expense = new Expense();

        expense.setExternalId(request.getExternalId());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setPaymentMethod(request.getPaymentMethod());

        return expense;
    }
}
