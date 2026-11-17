package com.aurev.backend.repository;

import com.aurev.backend.model.PurchaseLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseLedgerRepository extends JpaRepository<PurchaseLedger, Long> {
    List<PurchaseLedger> findByClientId(Long clientId);
    void deleteByClientId(Long clientId);
}
