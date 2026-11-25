package com.expensetracker.expense;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByOrderByDateDescIdDesc();

    @Query("select e.externalId from Expense e where e.externalId in :externalIds")
    Set<String> findExistingExternalIds(Collection<String> externalIds);

    @Modifying
    @Query("delete from Expense e where e.importBatchId = :importBatchId")
    int deleteByImportBatchId(Long importBatchId);

    @Modifying
    @Query("delete from Expense e where e.date between :from and :to")
    int deleteAllInPeriod(LocalDate from, LocalDate to);
}
