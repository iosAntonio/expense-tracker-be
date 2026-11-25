
package org.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import org.example.dto.ExpenseRequestDTO;
import org.example.dto.ExpenseResponseDTO;
import org.example.dto.ExpenseImportResponseDTO;
import org.example.service.ExpenseService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@Validated
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(
            @Valid @RequestBody ExpenseRequestDTO request) {

        ExpenseResponseDTO response = expenseService.createExpense(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ExpenseResponseDTO> getAllExpenses() {

        return expenseService.getAllExpenses();
    }

    @PostMapping("/import")
    public ResponseEntity<ExpenseImportResponseDTO> importExpenses(
            @RequestBody
            @NotEmpty(message = "La lista delle spese non può essere vuota")
            List<@Valid ExpenseRequestDTO> requests) {

        ExpenseImportResponseDTO response =
                expenseService.importExpenses(requests);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
