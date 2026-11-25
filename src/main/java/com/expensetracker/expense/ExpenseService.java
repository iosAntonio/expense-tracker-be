package com.expensetracker.expense;

import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expensetracker.config.ApiException;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findAll() {
        return repository.findAllByOrderByDateDescIdDesc().stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional
    public ExpenseResponse update(long id, ExpenseUpdateRequest request) {
        Expense expense = repository.findById(id).orElseThrow(ExpenseService::notFound);
        expense.update(
                request.date(),
                request.description(),
                request.amount(),
                request.category(),
                request.paymentMethod());
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public void delete(long id) {
        Expense expense = repository.findById(id).orElseThrow(ExpenseService::notFound);
        repository.delete(expense);
    }

    @Transactional
    public MonthDeletionResult deleteMonth(YearMonth month) {
        int deleted = repository.deleteAllInPeriod(month.atDay(1), month.atEndOfMonth());
        return new MonthDeletionResult(month.toString(), deleted);
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "Spesa non trovata.");
    }
}
