
package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.example.enums.ExpenseCategory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

public class ExpenseRequestDTO {

    @Size(max = 100, message = "L'identificativo esterno non può superare 100 caratteri")
    private String externalId;

    @NotNull(message = "La data è obbligatoria")
    private LocalDate date;

    @Size(max = 255, message = "La descrizione non può superare 255 caratteri")
    private String description;

    @NotNull(message = "L'importo è obbligatorio")
    @DecimalMin(value = "0.01", message = "L'importo deve essere maggiore di zero")
    @Digits(integer = 10, fraction = 2, message = "L'importo può avere massimo 2 decimali")
    private BigDecimal amount;

    @NotNull(message = "La categoria è obbligatoria")
    private ExpenseCategory category;

    @Size(max = 255, message = "Il metodo di pagamento non può superare 255 caratteri")
    private String paymentMethod;

    public String getExternalId() {
        return externalId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
