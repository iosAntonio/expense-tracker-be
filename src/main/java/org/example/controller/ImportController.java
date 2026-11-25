package org.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import org.example.dto.ExpenseRequestDTO;
import org.example.dto.ImportBatchDTO;
import org.example.dto.ImportCheckResponseDTO;
import org.example.dto.ImportRevertResponseDTO;
import org.example.service.ImportBatchService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/imports")
@Validated
public class ImportController {

    private final ImportBatchService importBatchService;

    public ImportController(ImportBatchService importBatchService) {
        this.importBatchService = importBatchService;
    }

    @GetMapping
    public List<ImportBatchDTO> getAllImports() {
        return importBatchService.getAllImports();
    }

    @PostMapping("/check")
    public ImportCheckResponseDTO checkImport(
            @RequestBody
            @NotEmpty(message = "La lista delle spese non può essere vuota")
            List<@Valid ExpenseRequestDTO> requests) {

        return importBatchService.findPreviousImports(requests);
    }

    @PostMapping("/{id}/revert")
    public ImportRevertResponseDTO revertImport(@PathVariable Long id) {
        return importBatchService.revert(id);
    }
}
