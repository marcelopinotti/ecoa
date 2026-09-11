package com.marcelodev.ecoa.expense;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseDraftRepository extends JpaRepository<ExpenseDraft, UUID> {

    Optional<ExpenseDraft> findFirstByOrderByCreatedAtDesc();
}
