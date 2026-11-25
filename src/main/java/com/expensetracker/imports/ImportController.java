package com.expensetracker.imports;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    private final ImportService service;

    public ImportController(ImportService service) {
        this.service = service;
    }

    @PostMapping
    public ImportResult importExpenses(@Valid @RequestBody ImportRequest request) {
        return service.importExpenses(request);
    }

    @PostMapping("/check")
    public ImportCheckResult check(@Valid @RequestBody ImportRequest request) {
        return service.check(request);
    }

    @GetMapping
    public List<ImportBatchResponse> findAll() {
        return service.findAll();
    }

    @PostMapping("/{id}/revert")
    public RevertResult revert(@PathVariable long id) {
        return service.revert(id);
    }
}
