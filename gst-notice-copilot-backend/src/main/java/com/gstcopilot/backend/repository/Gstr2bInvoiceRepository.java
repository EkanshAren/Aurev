package com.gstcopilot.backend.repository;

import com.gstcopilot.backend.entity.Gstr2bInvoice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Gstr2bInvoiceRepository extends JpaRepository<Gstr2bInvoice, Long> {
    List<Gstr2bInvoice> findByNoticeId(Long noticeId);
    void deleteByNoticeId(Long noticeId);
}
