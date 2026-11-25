package com.expensetracker.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "import_batch_id")
    private Long importBatchId;

    protected Expense() {
    }

    public Expense(String externalId, LocalDate date, String description, BigDecimal amount,
                   Category category, String paymentMethod, Long importBatchId) {
        this.externalId = externalId;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.importBatchId = importBatchId;
    }

    public void update(LocalDate date, String description, BigDecimal amount,
                       Category category, String paymentMethod) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.paymentMethod = paymentMethod;
    }

    public Long getId() {
        return id;
    }

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

    public Category getCategory() {
        return category;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public Long getImportBatchId() {
        return importBatchId;
    }
}
