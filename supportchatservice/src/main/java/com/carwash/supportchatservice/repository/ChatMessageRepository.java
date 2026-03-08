package com.carwash.supportchatservice.repository;

import com.carwash.supportchatservice.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // existing – used by polling APIs
    List<ChatMessage> findByPhoneAndIdGreaterThanOrderByIdAsc(String phone, Long id);

    // used by /chat/history
    List<ChatMessage> findBySessionIdOrderByIdAsc(Long sessionId);

    // extra helper (we’ll use it in getMessages to be extra safe)
    List<ChatMessage> findByPhoneOrderByIdAsc(String phone);
}
