package com.aurev.backend.repository;

import com.aurev.backend.model.GstNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GstNoticeRepository extends JpaRepository<GstNotice, String> {
}
