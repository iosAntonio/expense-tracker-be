package org.example.service;

import org.example.dto.ExpenseRequestDTO;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class ImportFingerprint {

    private static final String FIELD_SEPARATOR = "\u001f";

    private ImportFingerprint() {
    }

    /**
     * SHA-256 delle righe in forma canonica: lo stesso contenuto viene riconosciuto
     * anche con righe in un altro ordine, un'altra formattazione JSON o zeri finali negli importi.
     */
    static String of(List<ExpenseRequestDTO> rows) {
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

    private static String canonical(ExpenseRequestDTO row) {
        BigDecimal amount = row.getAmount();
        return String.join(FIELD_SEPARATOR,
                Objects.toString(row.getExternalId(), ""),
                Objects.toString(row.getDate(), ""),
                amount == null ? "" : amount.stripTrailingZeros().toPlainString(),
                Objects.toString(row.getCategory(), ""),
                Objects.toString(row.getDescription(), ""),
                Objects.toString(row.getPaymentMethod(), ""));
    }
}
