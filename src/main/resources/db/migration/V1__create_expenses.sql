CREATE TABLE expenses (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    external_id    VARCHAR(255)   NOT NULL,
    expense_date   DATE           NOT NULL,
    description    VARCHAR(255),
    amount         NUMERIC(12, 2) NOT NULL,
    category       VARCHAR(30)    NOT NULL,
    payment_method VARCHAR(255),
    CONSTRAINT uq_expenses_external_id UNIQUE (external_id),
    CONSTRAINT ck_expenses_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_expenses_category CHECK (category IN (
        'ALIMENTARI', 'BAR_RISTORAZIONE', 'CASA', 'AUTO', 'TRASPORTI', 'VIAGGI',
        'SPORT', 'SVAGO', 'SHOPPING', 'SALUTE', 'ALTRO'
    ))
);

CREATE INDEX ix_expenses_expense_date ON expenses (expense_date);
