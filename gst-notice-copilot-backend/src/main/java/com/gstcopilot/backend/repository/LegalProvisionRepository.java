package com.gstcopilot.backend.repository;

import com.gstcopilot.backend.entity.LegalProvision;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalProvisionRepository extends JpaRepository<LegalProvision, Long> {

    Optional<LegalProvision> findByCitationId(String citationId);

    List<LegalProvision> findByCitationIdIn(Collection<String> citationIds);

    /**
     * Native MySQL FULLTEXT natural language search fallback when vector embeddings
     * are not configured (e.g., cloud Groq chat-only mode without an embedding endpoint).
     */
    @Query(value = """
            SELECT lp.*
            FROM legal_provisions lp
            WHERE MATCH(lp.title, lp.section_ref, lp.summary, lp.full_text)
                  AGAINST(:searchQuery IN NATURAL LANGUAGE MODE)
               OR lp.applicable_forms LIKE CONCAT('%', :formType, '%')
            ORDER BY
                MATCH(lp.title, lp.section_ref, lp.summary, lp.full_text)
                AGAINST(:searchQuery IN NATURAL LANGUAGE MODE) DESC,
                CASE WHEN lp.applicable_forms LIKE CONCAT('%', :formType, '%') THEN 1 ELSE 0 END DESC
            LIMIT :topK
            """, nativeQuery = true)
    List<LegalProvision> searchFullTextTopK(
            @Param("searchQuery") String searchQuery,
            @Param("formType") String formType,
            @Param("topK") int topK);
}
