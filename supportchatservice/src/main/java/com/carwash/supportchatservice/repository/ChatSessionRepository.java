package com.carwash.supportchatservice.repository;

import com.carwash.supportchatservice.entity.ChatSession;
import com.carwash.supportchatservice.entity.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // latest non-closed session for given phone
    Optional<ChatSession> findTopByPhoneAndStatusInOrderByStartedAtDesc(
            String phone,
            List<ChatStatus> statuses
    );

    // sessions for a phone within a time window (for history)
    List<ChatSession> findByPhoneAndStartedAtAfterOrderByStartedAtDesc(
            String phone,
            LocalDateTime after
    );

    // active / closed sessions for admin
    List<ChatSession> findByStatusInAndStartedAtAfterOrderByLastActivityAtDesc(
            List<ChatStatus> statuses,
            LocalDateTime after
    );
   
}
