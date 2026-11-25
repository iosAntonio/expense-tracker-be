package com.expensetracker.imports;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.expensetracker.expense.ExpenseImportRequest;

final class ImportFingerprint {

    private static final String FIELD_SEPARATOR = "\u001f";

    private ImportFingerprint() {
    }

    /**
     * SHA-256 of the rows in a canonical form, so the same content is recognised
     * regardless of row order, JSON formatting or trailing zeros in amounts.
     */
    static String of(List<ExpenseImportRequest> rows) {
        String canonical = rows.stream()
                .map(ImportFingerprint::canonical)
                .sorted()
                .collect(Collectors.joining("\n"));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 non disponibile", e);
        }
    }

    private static String canonical(ExpenseImportRequest row) {
        return String.join(FIELD_SEPARATOR,
                row.externalId().trim(),
                row.date().toString(),
                row.amount().stripTrailingZeros().toPlainString(),
                row.category().name(),
                Objects.toString(row.description(), ""),
                Objects.toString(row.paymentMethod(), ""));
    }
}
