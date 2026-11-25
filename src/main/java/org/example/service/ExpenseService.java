
package org.example.service;

import org.example.dto.ExpenseRequestDTO;
import org.example.dto.ExpenseResponseDTO;
import org.example.dto.ExpenseImportResponseDTO;
import org.example.entity.Expense;
import org.example.repository.ExpenseRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO request) {

        Expense expense = convertToEntity(request);

        Expense savedExpense = expenseRepository.save(expense);

        return convertToResponseDTO(savedExpense);
    }

    @Transactional
    public ExpenseImportResponseDTO importExpenses(
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

        List<Expense> newExpenses = requests.stream()
                .filter(request -> !existingIds.contains(request.getExternalId()))
                .map(this::convertToEntity)
                .toList();

        List<Expense> savedExpenses = expenseRepository.saveAll(newExpenses);

        List<ExpenseResponseDTO> importedExpenses = savedExpenses.stream()
                .map(this::convertToResponseDTO)
                .toList();

        return new ExpenseImportResponseDTO(
                requests.size(),
                savedExpenses.size(),
                existingIds.size(),
                importedExpenses
        );
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getAllExpenses() {

        return expenseRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
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
