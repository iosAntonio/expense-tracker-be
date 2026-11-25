# Expense Tracker: back-end

API REST in Spring Boot 3.5 e Java 21, con PostgreSQL e migrazioni Flyway.

Il front-end è in [expense-tracker-fe](https://github.com/iosAntonio/expense-tracker-fe).

## Board dei task

Tutti i task del progetto, front-end e back-end, sono sulla board: https://claude.ai/artifact/BA5NjWm9KAbVcEfcdq7uye
Per aprirla serve che il proprietario te l'abbia condivisa.

Istantanea del 25/11/2026. Lo stato aggiornato è sempre sulla board.

### Da fare (1)

| Task | Titolo | Tipo | Area | Priorità |
|---|---|---|---|---|
| ET-30 | Concordare il workflow Git per i due repository | Task | Product | Alta |

### In corso (1)

| Task | Titolo | Tipo | Area | Priorità |
|---|---|---|---|---|
| ET-41 | Committare e aprire le PR del lavoro fatto finora | Task | DevOps | Massima |

### In review (27)

| Task | Titolo | Tipo | Area | Priorità |
|---|---|---|---|---|
| ET-1 | Backend iniziale: API spese con Postgres e Flyway | Storia | BE | Alta |
| ET-2 | Ambiente locale con Docker per FE, BE e database | Task | DevOps | Alta |
| ET-4 | La suite di test del front-end non compila | Bug | FE | Massima |
| ET-5 | La build di produzione del front-end non compila | Bug | FE | Alta |
| ET-6 | KPI della dashboard con valori finti fissi nel template | Bug | FE | Alta |
| ET-7 | Le spese prima di ottobre 2026 non si possono consultare | Bug | FE | Media |
| ET-8 | Import: dire all'utente quante spese erano già presenti | Bug | FE | Media |
| ET-14 | Togliere .claude/settings.local.json dal repository | Debito tecnico | FE | Media |
| ET-15 | Import: risposta con il riepilogo di spese importate e saltate | Task | BE | Media |
| ET-19 | Test di integrazione del back-end | Task | BE | Alta |
| ET-31 | Registro degli import con impronta del contenuto | Storia | BE | Alta |
| ET-32 | Annullare un import in blocco | Storia | BE | Alta |
| ET-33 | Avviso «file già importato» nell'anteprima dell'import | Storia | FE | Alta |
| ET-34 | Sezione «Import recenti» con annullamento | Storia | FE | Alta |
| ET-35 | Toast di esito per import e annullamento | Storia | FE | Media |
| ET-36 | API per modificare ed eliminare spese, anche un mese intero | Storia | BE | Alta |
| ET-37 | Modificare ed eliminare una spesa dalla tabella | Storia | FE | Alta |
| ET-38 | Eliminare tutte le spese del mese selezionato | Storia | FE | Alta |
| ET-42 | Navigare per anno e mese, anche fra anni diversi | Storia | FE | Alta |
| ET-43 | Vista annuale con l'andamento dei 12 mesi | Storia | FE | Alta |
| ET-44 | Dati e test multi-anno 2025-2026 | Task | Product | Media |
| ET-12 | Calcoli della dashboard ripetuti a ogni change detection | Debito tecnico | FE | Alta |
| ET-45 | Pagina Dashboard iniziale con andamento e ripartizione per categoria | Storia | FE | Alta |
| ET-11 | Un unico modello Categoria condiviso nel front-end | Debito tecnico | FE | Media |
| ET-9 | Colori delle categorie duplicati nel grafico | Bug | FE | Media |
| ET-46 | Filtrare per una o più categorie in Dashboard e Spese | Storia | FE | Alta |
| ET-47 | Ricostruire le funzionalità sul back-end di Antonio (develop) | Task | BE | Massima |

### Backlog (17)

| Task | Titolo | Tipo | Area | Priorità |
|---|---|---|---|---|
| ET-22 | Entrate e investimenti: modello dati e KPI reali | Storia | Product | Alta |
| ET-39 | Anteprima import: segnalare i possibili duplicati | Storia | Product | Media |
| ET-16 | Import concorrenti con lo stesso externalId restituiscono 500 | Bug | BE | Media |
| ET-18 | Limite di dimensione per l'import | Task | BE | Media |
| ET-20 | Filtri e paginazione lato server | Storia | BE | Media |
| ET-21 | Aggiungere una spesa a mano | Storia | Product | Media |
| ET-23 | Autenticazione e spese per utente | Storia | Product | Media |
| ET-24 | Aggiornare Angular 13 a una versione supportata | Debito tecnico | FE | Media |
| ET-25 | Pipeline CI su GitHub Actions | Task | DevOps | Media |
| ET-10 | Pulizia del template e del codice inutilizzato | Debito tecnico | FE | Bassa |
| ET-13 | Applicare Prettier e uniformare lo stile del codice | Debito tecnico | FE | Bassa |
| ET-17 | Messaggi di errore del BE in italiano e con il numero di riga | Task | BE | Bassa |
| ET-26 | Docker: un solo comando per avviare tutto e container più sicuri | Task | DevOps | Bassa |
| ET-27 | Documentazione API con OpenAPI e README aggiornati | Task | BE | Bassa |
| ET-28 | Decidere quale mese mostrare all'apertura della dashboard | Task | Product | Bassa |
| ET-29 | File JSON di esempio scaricabile nella pagina Import | Storia | FE | Bassa |
| ET-40 | Il registro degli import non tiene conto di modifiche ed eliminazioni | Bug | BE | Bassa |

### Fatto (1)

| Task | Titolo | Tipo | Area | Priorità |
|---|---|---|---|---|
| ET-3 | Code review iniziale di front-end e back-end | Task | Product | Media |

## Avvio con Docker

`docker compose up -d --build` avvia:

- PostgreSQL 17 su `127.0.0.1:5433` (database `expense_tracker`; utente e password `expense`, solo per lo sviluppo locale)
- l'API su http://localhost:8080 (stato del servizio: `/api/health`)

## Avvio dall'IDE

Le impostazioni sono in `src/main/resources/application.yml` e si cambiano con le variabili `DB_URL`, `DB_USER` e `DB_PASSWORD`. Una `src/main/resources/application.properties` locale, esclusa da git, ha la precedenza.

Lo schema lo creano le migrazioni Flyway. Un database creato in passato da Hibernate viene riconosciuto come versione 1 e riceve solo le migrazioni successive.

## Test

`docker compose run --rm test` esegue i test di integrazione con Testcontainers: usa il Docker della macchina per avviare un PostgreSQL temporaneo.

## API

| Metodo | Percorso | Cosa fa |
|---|---|---|
| GET | `/api/health` | Stato del servizio |
| GET | `/api/expenses` | Tutte le spese, dalla più recente |
| POST | `/api/expenses` | Crea una spesa |
| PUT | `/api/expenses/{id}` | Modifica una spesa; l'`externalId` resta invariato |
| DELETE | `/api/expenses/{id}` | Elimina una spesa |
| DELETE | `/api/expenses?month=AAAA-MM` | Elimina tutte le spese del mese |
| POST | `/api/expenses/import?fileName=…` | Importa una lista di spese, salta gli `externalId` già presenti e registra l'import |
| GET | `/api/imports` | Registro degli import |
| POST | `/api/imports/check` | Dice se lo stesso contenuto è già stato importato, senza salvare nulla |
| POST | `/api/imports/{id}/revert` | Annulla un import cancellandone le spese |

Esempi di richieste in `expense.http`.
