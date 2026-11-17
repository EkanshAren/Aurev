package com.aurev.backend.repository;

import com.aurev.backend.model.LegalCitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalCitationRepository extends JpaRepository<LegalCitation, String> {
}
