package org.example.repository;

import org.example.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByExternalIdIn(Collection<String> externalIds);
}
