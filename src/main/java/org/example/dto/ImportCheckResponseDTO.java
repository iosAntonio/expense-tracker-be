package org.example.dto;

import java.util.List;

public class ImportCheckResponseDTO {

    private List<ImportBatchDTO> previousImports;

    public ImportCheckResponseDTO() {
    }

    public ImportCheckResponseDTO(List<ImportBatchDTO> previousImports) {
        this.previousImports = previousImports;
    }

    public List<ImportBatchDTO> getPreviousImports() {
        return previousImports;
    }

    public void setPreviousImports(List<ImportBatchDTO> previousImports) {
        this.previousImports = previousImports;
    }
}
