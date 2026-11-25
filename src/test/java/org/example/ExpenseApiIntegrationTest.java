package org.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.entity.Expense;
import org.example.enums.ExpenseCategory;
import org.example.repository.ExpenseRepository;
import org.example.repository.ImportBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ExpenseApiIntegrationTest {

    private static final String FRONTEND_ORIGIN = "http://localhost:4200";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenses;

    @Autowired
    private ImportBatchRepository batches;

    @BeforeEach
    void cleanDatabase() {
        expenses.deleteAll();
        batches.deleteAll();
    }

    @Test
    void healthEndpointAnswers() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Expense Tracker API is running!"));
    }

    @Test
    void createsASingleExpenseWithoutExternalId() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date": "2026-11-12", "description": "Pranzo", "amount": 15.50,
                                 "category": "BAR_RISTORAZIONE", "paymentMethod": "Carta"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.amount").value(15.5));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(jsonPath("$[*].description", contains("Pranzo")));
        assertThat(expenses.findAll().get(0).getExternalId()).isNull();
    }

    @Test
    void createExplainsWhatIsMissing() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date": "2026-11-12", "description": "Cena", "amount": 25.00}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.category").value("La categoria è obbligatoria"))
                .andExpect(jsonPath("$.message", containsString("La categoria è obbligatoria")));
    }

    @Test
    void listsExpensesOfDifferentYearsNewestFirst() throws Exception {
        expenses.saveAll(List.of(
                expense("a", "2025-12-31"),
                expense("b", "2026-01-01"),
                expense("c", "2025-01-01"),
                expense("d", "2026-12-31")));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(jsonPath("$[*].date",
                        contains("2026-12-31", "2026-01-01", "2025-12-31", "2025-01-01")));
    }

    @Test
    void updateChangesTheExpenseButKeepsItsExternalId() throws Exception {
        Expense saved = expenses.save(expense("a-1", "2026-10-02"));

        mockMvc.perform(put("/api/expenses/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"externalId": "ignorato", "date": "2026-11-03", "description": "Spesa corretta",
                                 "amount": 40.1, "category": "CASA", "paymentMethod": null}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.date").value("2026-11-03"))
                .andExpect(jsonPath("$.description").value("Spesa corretta"))
                .andExpect(jsonPath("$.amount").value(40.1))
                .andExpect(jsonPath("$.category").value("CASA"))
                .andExpect(jsonPath("$.paymentMethod", nullValue()));

        Expense reloaded = expenses.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getExternalId()).isEqualTo("a-1");
        assertThat(reloaded.getAmount()).isEqualByComparingTo("40.10");
    }

    @Test
    void updateRejectsInvalidDataAndUnknownExpenses() throws Exception {
        Expense saved = expenses.save(expense("a-1", "2026-10-02"));

        mockMvc.perform(put("/api/expenses/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date": "2026-10-02", "amount": 0, "category": "CASA"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("L'importo deve essere maggiore di zero")));

        mockMvc.perform(put("/api/expenses/{id}", 999_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date": "2026-10-02", "amount": 1, "category": "CASA"}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Spesa non trovata."));

        assertThat(expenses.findById(saved.getId()).orElseThrow().getAmount()).isEqualByComparingTo("10");
    }

    @Test
    void deleteRemovesASingleExpense() throws Exception {
        Expense first = expenses.save(expense("a-1", "2026-10-02"));
        expenses.save(expense("a-2", "2026-10-05"));

        mockMvc.perform(delete("/api/expenses/{id}", first.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/expenses"))
                .andExpect(jsonPath("$[*].date", contains("2026-10-05")));
        mockMvc.perform(delete("/api/expenses/{id}", first.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingDecember2025KeepsDecember2026AndTheNeighbouringMonths() throws Exception {
        expenses.saveAll(List.of(
                expense("nov-25", "2025-11-30"),
                expense("dic-25-a", "2025-12-01"),
                expense("dic-25-b", "2025-12-31"),
                expense("gen-26", "2026-01-01"),
                expense("dic-26", "2026-12-15")));

        mockMvc.perform(delete("/api/expenses").param("month", "2025-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2025-12"))
                .andExpect(jsonPath("$.deletedCount").value(2));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(jsonPath("$[*].date", contains("2026-12-15", "2026-01-01", "2025-11-30")));
    }

    @Test
    void deleteMonthRequiresAValidMonth() throws Exception {
        expenses.save(expense("o-01", "2026-10-01"));

        mockMvc.perform(delete("/api/expenses"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parametro obbligatorio mancante: month."));
        mockMvc.perform(delete("/api/expenses").param("month", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valore non valido per il parametro month."));
        mockMvc.perform(delete("/api/expenses").param("month", "ottobre"))
                .andExpect(status().isBadRequest());

        assertThat(expenses.count()).isEqualTo(1);
    }

    @Test
    void corsAllowsOnlyTheFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/expenses/1")
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_ORIGIN));

        mockMvc.perform(get("/api/expenses").header(HttpHeaders.ORIGIN, "http://evil.example"))
                .andExpect(status().isForbidden());
    }

    private static Expense expense(String externalId, String date) {
        Expense expense = new Expense();
        expense.setExternalId(externalId);
        expense.setDate(LocalDate.parse(date));
        expense.setAmount(new BigDecimal("10.00"));
        expense.setCategory(ExpenseCategory.ALTRO);
        return expense;
    }
}
