package org.example.repository;

import org.example.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByExternalIdIn(Collection<String> externalIds);

    @Modifying
    @Query("delete from Expense e where e.importBatchId = :importBatchId")
    int deleteByImportBatchId(@Param("importBatchId") Long importBatchId);

    @Modifying
    @Query("delete from Expense e where e.date between :from and :to")
    int deleteAllInPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
