CREATE TABLE import_batches (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    file_name      VARCHAR(255)             NOT NULL,
    content_hash   VARCHAR(64)              NOT NULL,
    imported_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    row_count      INTEGER                  NOT NULL,
    imported_count INTEGER                  NOT NULL,
    skipped_count  INTEGER                  NOT NULL,
    reverted_at    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX ix_import_batches_content_hash ON import_batches (content_hash);

ALTER TABLE expenses ADD COLUMN import_batch_id BIGINT REFERENCES import_batches (id);

CREATE INDEX ix_expenses_import_batch_id ON expenses (import_batch_id);
