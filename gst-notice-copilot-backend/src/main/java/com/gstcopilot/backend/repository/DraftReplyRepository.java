package com.gstcopilot.backend.repository;

import com.gstcopilot.backend.entity.DraftReply;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DraftReplyRepository extends JpaRepository<DraftReply, Long> {
    List<DraftReply> findByNoticeIdOrderByCreatedAtDesc(Long noticeId);
    Optional<DraftReply> findFirstByNoticeIdOrderByCreatedAtDesc(Long noticeId);
}
