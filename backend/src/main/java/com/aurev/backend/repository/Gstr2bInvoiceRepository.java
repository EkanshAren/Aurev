package com.aurev.backend.repository;

import com.aurev.backend.model.Gstr2bInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface Gstr2bInvoiceRepository extends JpaRepository<Gstr2bInvoice, Long> {
    List<Gstr2bInvoice> findByClientId(Long clientId);
    void deleteByClientId(Long clientId);
}
