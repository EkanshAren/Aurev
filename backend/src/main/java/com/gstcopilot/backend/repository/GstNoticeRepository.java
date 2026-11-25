package com.gstcopilot.backend.repository;

import com.gstcopilot.backend.entity.GstNotice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GstNoticeRepository extends JpaRepository<GstNotice, Long> {
    Optional<GstNotice> findByDin(String din);
}
