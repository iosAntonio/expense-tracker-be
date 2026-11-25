package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.example.enums.ExpenseCategory;
public class ExpenseResponseDTO {

    private Long id;
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private ExpenseCategory category;
    private String paymentMethod;

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
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

    // Getter e Setter
}