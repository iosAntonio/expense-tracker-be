package org.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.repository.ExpenseRepository;
import org.example.repository.ImportBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ImportApiIntegrationTest {

    private static final String OCTOBER_1 = """
            {"externalId": "ott-1", "date": "2026-10-02", "description": "Spesa settimanale",
             "amount": 42.5, "category": "ALIMENTARI", "paymentMethod": "Carta"}""";
    private static final String OCTOBER_2 = """
            {"externalId": "ott-2", "date": "2026-10-05", "amount": 12, "category": "BAR_RISTORAZIONE"}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void importSavesTheExpensesAndRecordsTheImport() throws Exception {
        importRows("ottobre.json", OCTOBER_1, OCTOBER_2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceived").value(2))
                .andExpect(jsonPath("$.importedCount").value(2))
                .andExpect(jsonPath("$.alreadyImportedCount").value(0))
                .andExpect(jsonPath("$.importedExpenses[*].date",
                        containsInAnyOrder("2026-10-02", "2026-10-05")))
                .andExpect(jsonPath("$.skippedExternalIds", empty()))
                .andExpect(jsonPath("$.batch.fileName").value("ottobre.json"))
                .andExpect(jsonPath("$.batch.rowCount").value(2))
                .andExpect(jsonPath("$.batch.importedCount").value(2))
                .andExpect(jsonPath("$.batch.revertedAt", nullValue()));

        mockMvc.perform(get("/api/imports"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileName").value("ottobre.json"));
    }

    @Test
    void importWithoutFileNameKeepsWorkingAndUsesADefaultName() throws Exception {
        mockMvc.perform(post("/api/expenses/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + OCTOBER_1 + "]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batch.fileName").value("import.json"));
    }

    @Test
    void importingOctoberTwiceDoesNotDuplicateAnythingAndIsNotRecordedAgain() throws Exception {
        importRows("ottobre.json", OCTOBER_1, OCTOBER_2).andExpect(status().isOk());

        importRows("ottobre-copia.json", OCTOBER_1, OCTOBER_2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(0))
                .andExpect(jsonPath("$.alreadyImportedCount").value(2))
                .andExpect(jsonPath("$.batch", nullValue()))
                .andExpect(jsonPath("$.skippedExternalIds", containsInAnyOrder("ott-1", "ott-2")));

        assertThat(expenses.count()).isEqualTo(2);
        mockMvc.perform(get("/api/imports")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void importSkipsOnlyTheExpensesAlreadyStored() throws Exception {
        importRows("ottobre.json", OCTOBER_1).andExpect(status().isOk());

        importRows("ottobre-aggiornato.json", OCTOBER_1, OCTOBER_2)
                .andExpect(jsonPath("$.importedCount").value(1))
                .andExpect(jsonPath("$.alreadyImportedCount").value(1))
                .andExpect(jsonPath("$.skippedExternalIds", contains("ott-1")))
                .andExpect(jsonPath("$.batch.rowCount").value(2))
                .andExpect(jsonPath("$.batch.importedCount").value(1))
                .andExpect(jsonPath("$.batch.skippedCount").value(1));

        assertThat(expenses.count()).isEqualTo(2);
    }

    @Test
    void importRejectsMissingOrRepeatedExternalIdsWithABadRequest() throws Exception {
        importRows("ottobre.json", OCTOBER_1, OCTOBER_2, OCTOBER_2)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Il JSON contiene externalId duplicati: ott-2"));

        importRows("ottobre.json", """
                {"date": "2026-10-05", "amount": 12, "category": "ALTRO"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ogni spesa deve avere un externalId"));

        assertThat(expenses.count()).isZero();
        assertThat(batches.count()).isZero();
    }

    @Test
    void checkRecognisesTheSameContentEvenIfReorderedOrFormattedDifferently() throws Exception {
        importRows("ottobre.json", OCTOBER_1, OCTOBER_2).andExpect(status().isOk());

        String reformatted = OCTOBER_1.replace("42.5", "42.50").replace("\n", " ");
        checkRows(OCTOBER_2, reformatted)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousImports", hasSize(1)))
                .andExpect(jsonPath("$.previousImports[0].fileName").value("ottobre.json"))
                .andExpect(jsonPath("$.previousImports[0].importedAt", notNullValue()));

        checkRows(OCTOBER_1, OCTOBER_2.replace("12", "13"))
                .andExpect(jsonPath("$.previousImports", empty()));
        assertThat(expenses.count()).isEqualTo(2);
    }

    @Test
    void revertRemovesOnlyTheExpensesOfThatImport() throws Exception {
        long october = batchId(importRows("ottobre.json", OCTOBER_1, OCTOBER_2));
        importRows("novembre.json", """
                {"externalId": "nov-1", "date": "2026-11-03", "amount": 20, "category": "SVAGO"}""");

        mockMvc.perform(post("/api/imports/{id}/revert", october))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.removedCount").value(2))
                .andExpect(jsonPath("$.batch.revertedAt", notNullValue()));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(jsonPath("$[*].date", contains("2026-11-03")));
        mockMvc.perform(get("/api/imports"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].fileName").value("ottobre.json"))
                .andExpect(jsonPath("$[1].revertedAt", notNullValue()));
    }

    @Test
    void aRevertedFileIsNoLongerReportedAndCanBeImportedAgain() throws Exception {
        long october = batchId(importRows("ottobre.json", OCTOBER_1, OCTOBER_2));
        mockMvc.perform(post("/api/imports/{id}/revert", october)).andExpect(status().isOk());

        checkRows(OCTOBER_1, OCTOBER_2).andExpect(jsonPath("$.previousImports", empty()));
        importRows("ottobre.json", OCTOBER_1, OCTOBER_2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(2));
        assertThat(expenses.count()).isEqualTo(2);
    }

    @Test
    void revertingTwiceIsAConflictAndAnUnknownImportIsNotFound() throws Exception {
        long october = batchId(importRows("ottobre.json", OCTOBER_1));
        mockMvc.perform(post("/api/imports/{id}/revert", october)).andExpect(status().isOk());

        mockMvc.perform(post("/api/imports/{id}/revert", october))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Questo import è già stato annullato."));
        mockMvc.perform(post("/api/imports/{id}/revert", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Import non trovato."));
    }

    @Test
    void reimportingTheSameFileDoesNotOverwriteAnExpenseCorrectedByHand() throws Exception {
        importRows("ottobre.json", OCTOBER_1).andExpect(status().isOk());
        long id = expenses.findAll().get(0).getId();
        mockMvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date": "2026-10-02", "description": "Spesa corretta", "amount": 50,
                                 "category": "ALIMENTARI", "paymentMethod": "Carta"}"""))
                .andExpect(status().isOk());

        importRows("ottobre.json", OCTOBER_1)
                .andExpect(jsonPath("$.skippedExternalIds", contains("ott-1")));

        assertThat(expenses.findById(id).orElseThrow().getAmount()).isEqualByComparingTo("50");
    }

    @Test
    void importAcceptsExpensesAcrossYearsAndRejectsDaysThatDoNotExist() throws Exception {
        importRows("2024-2026.json",
                row("y-1", "2024-02-29"),
                row("y-2", "2025-02-28"),
                row("y-3", "2025-12-31"),
                row("y-4", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(4));

        importRows("non-bisestile.json", row("y-5", "2025-02-29")).andExpect(status().isBadRequest());
        importRows("non-bisestile.json", row("y-6", "2026-02-29")).andExpect(status().isBadRequest());

        assertThat(expenses.count()).isEqualTo(4);
    }

    @Test
    void importRejectsInvalidRowsAnEmptyListAndUnknownCategories() throws Exception {
        importRows("ottobre.json", OCTOBER_1, """
                {"externalId": "bad-1", "date": "2026-10-01", "amount": -5, "category": "ALTRO"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("L'importo deve essere maggiore di zero")));
        importRows("ottobre.json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("La lista delle spese non può essere vuota")));
        importRows("ottobre.json", OCTOBER_1.replace("ALIMENTARI", "PIZZA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Il formato della richiesta non è valido")));

        assertThat(expenses.count()).isZero();
        assertThat(batches.count()).isZero();
    }

    private ResultActions importRows(String fileName, String... rows) throws Exception {
        return mockMvc.perform(withRows(post("/api/expenses/import").param("fileName", fileName), rows));
    }

    private ResultActions checkRows(String... rows) throws Exception {
        return mockMvc.perform(withRows(post("/api/imports/check"), rows));
    }

    private long batchId(ResultActions result) throws Exception {
        JsonNode json = objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
        return json.path("batch").path("id").asLong();
    }

    private static MockHttpServletRequestBuilder withRows(MockHttpServletRequestBuilder request, String... rows) {
        return request
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + String.join(",", rows) + "]");
    }

    private static String row(String externalId, String date) {
        return "{\"externalId\": \"" + externalId + "\", \"date\": \"" + date
                + "\", \"amount\": 10, \"category\": \"ALTRO\"}";
    }
}
