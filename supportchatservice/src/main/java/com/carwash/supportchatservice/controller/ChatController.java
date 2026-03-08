package com.carwash.supportchatservice.controller;

import com.carwash.supportchatservice.dto.*;
import com.carwash.supportchatservice.entity.ChatMessage;
import com.carwash.supportchatservice.entity.ChatSession;
import com.carwash.supportchatservice.entity.ChatStatus;
import com.carwash.supportchatservice.repository.ChatMessageRepository;
import com.carwash.supportchatservice.repository.ChatSessionRepository;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatMessageRepository chatRepo;
    private final ChatSessionRepository sessionRepo;

    public ChatController(ChatMessageRepository chatRepo,
                          ChatSessionRepository sessionRepo) {
        this.chatRepo = chatRepo;
        this.sessionRepo = sessionRepo;
    }

    /* ========== 1) START CHAT SESSION (optional) ========== */

    @PostMapping("/start")
    public ResponseEntity<StartChatResponse> startChat(@RequestBody StartChatRequest req) {
        if (req.getPhone() == null || req.getPhone().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ChatSession session = new ChatSession();
        session.setPhone(req.getPhone());
        session.setUsername(req.getUsername());
        session.setStatus(ChatStatus.NEW);     // user requested, not yet active
        session = sessionRepo.save(session);

        if (req.getFirstMessage() != null && !req.getFirstMessage().isBlank()) {
            ChatMessage msg = new ChatMessage();
            msg.setPhone(req.getPhone());
            msg.setUsername(req.getUsername());
            msg.setSender("USER");
            msg.setMessage(req.getFirstMessage());
            msg.setSessionId(session.getId());

            ChatMessage saved = chatRepo.save(msg);

            session.setLastActivityAt(saved.getCreatedAt());
            sessionRepo.save(session);
        }

        StartChatResponse resp =
                new StartChatResponse(session.getId(), session.getPhone(), session.getStatus());
        return ResponseEntity.ok(resp);
    }

    /* Helper: latest open session (NEW or ACTIVE) or create NEW */

    private ChatSession getOrCreateOpenSession(String phone, String username) {
        List<ChatStatus> openStatuses = Arrays.asList(ChatStatus.NEW, ChatStatus.ACTIVE);

        Optional<ChatSession> existing =
                sessionRepo.findTopByPhoneAndStatusInOrderByStartedAtDesc(phone, openStatuses);

        if (existing.isPresent()) {
            return existing.get();
        }

        ChatSession s = new ChatSession();
        s.setPhone(phone);
        s.setUsername(username);
        s.setStatus(ChatStatus.NEW);   // requested
        return sessionRepo.save(s);
    }

    /* ========== 2) USER SENDING MESSAGE ========== */

    @PostMapping("/user-message")
    public ResponseEntity<ChatMessage> userMessage(
            @RequestBody ChatMessageRequest req,
            @RequestParam(name = "sessionId", required = false) Long sessionId) {

        if (req.getPhone() == null || req.getPhone().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (req.getMessage() == null || req.getMessage().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ChatSession session;
        if (sessionId != null) {
            session = sessionRepo.findById(sessionId)
                    .orElseGet(() -> getOrCreateOpenSession(req.getPhone(), req.getUsername()));
        } else {
            session = getOrCreateOpenSession(req.getPhone(), req.getUsername());
        }

        ChatMessage msg = new ChatMessage();
        msg.setPhone(req.getPhone());
        msg.setUsername(req.getUsername());
        msg.setSender("USER");
        msg.setMessage(req.getMessage());
        msg.setSessionId(session.getId());

        ChatMessage saved = chatRepo.save(msg);

        // 👉 IMPORTANT: user messages DO NOT change status to ACTIVE
        // If session is NEW, keep it NEW (requested).
        session.setLastActivityAt(saved.getCreatedAt());
        sessionRepo.save(session);

        return ResponseEntity.ok(saved);
    }

    /* ========== 3) ADMIN SENDING MESSAGE ========== */

    @PostMapping("/admin-message")
    public ResponseEntity<ChatMessage> adminMessage(
            @RequestBody ChatMessageRequest req,
            @RequestParam(name = "sessionId", required = false) Long sessionId) {

        if (req.getPhone() == null || req.getPhone().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (req.getMessage() == null || req.getMessage().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String phone    = req.getPhone();
        String username = req.getUsername();
        String text     = req.getMessage();

        ChatSession session;
        if (sessionId != null) {
            session = sessionRepo.findById(sessionId)
                    .orElseGet(() -> getOrCreateOpenSession(phone, username));
        } else {
            session = getOrCreateOpenSession(phone, username);
        }

        ChatMessage msg = new ChatMessage();
        msg.setPhone(phone);
        msg.setUsername(username);
        msg.setSender("ADMIN");
        msg.setMessage(text);
        msg.setSessionId(session.getId());

        ChatMessage saved = chatRepo.save(msg);

        // --- session status logic ---
        // admin window sends this exact text on End Chat
        boolean isEnd = "Admin has ended the chat"
                .equalsIgnoreCase(text);

        if (isEnd) {
            session.setStatus(ChatStatus.CLOSED);
            session.setEndedBy("ADMIN");
        } else {
            // any normal admin message means chat is active
            session.setStatus(ChatStatus.ACTIVE);
        }

        session.setLastActivityAt(saved.getCreatedAt());
        sessionRepo.save(session);

        return ResponseEntity.ok(saved);
    }

    /* ========== 4) POLLING (same as before) ========== */

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @RequestParam String phone,
            @RequestParam(name = "afterId", defaultValue = "0") Long afterId) {

        List<ChatMessage> list =
                chatRepo.findByPhoneAndIdGreaterThanOrderByIdAsc(phone, afterId);

        return ResponseEntity.ok(list);
    }

    /* ========== 5) MARK SESSION ACTIVE WHEN ADMIN CLICKS ========== */

    @PostMapping("/sessions/activate")
    public ResponseEntity<Void> activateSession(@RequestBody ActivateSessionRequest req) {
        if (req.getPhone() == null || req.getPhone().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatStatus> openStatuses = Arrays.asList(ChatStatus.NEW, ChatStatus.ACTIVE);
        ChatSession session = sessionRepo
                .findTopByPhoneAndStatusInOrderByStartedAtDesc(req.getPhone(), openStatuses)
                .orElse(null);

        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        if (session.getStatus() != ChatStatus.ACTIVE) {
            session.setStatus(ChatStatus.ACTIVE);
        }
        session.setLastActivityAt(LocalDateTime.now());
        sessionRepo.save(session);

        return ResponseEntity.ok().build();
    }

    /* ========== 6) END SESSION (USER or ADMIN) ========== */

    @PostMapping("/end")
    public ResponseEntity<Void> endChat(@RequestBody EndChatRequest req) {
    	System.out.println("[END CHAT] phone=" + req.getPhone()
        + ", sessionId=" + req.getSessionId()
        + ", endedBy=" + req.getEndedBy());

        ChatSession session = null;

        if (req.getSessionId() != null) {
            session = sessionRepo.findById(req.getSessionId()).orElse(null);
        }

        if (session == null && req.getPhone() != null) {
            List<ChatStatus> openStatuses = Arrays.asList(ChatStatus.NEW, ChatStatus.ACTIVE);
            session = sessionRepo
                    .findTopByPhoneAndStatusInOrderByStartedAtDesc(req.getPhone(), openStatuses)
                    .orElse(null);
        }

        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        session.setStatus(ChatStatus.CLOSED);
        session.setEndedBy(req.getEndedBy());            // "USER" or "ADMIN"
        session.setLastActivityAt(LocalDateTime.now());
        sessionRepo.save(session);
        System.out.println("[END CHAT] Closed session " + session.getId()
        + " endedBy=" + session.getEndedBy());
        return ResponseEntity.ok().build();
    }

    /* ========== 7) USER CHAT HISTORY (30 days) ========== */

    @GetMapping("/history")
    public ResponseEntity<List<ChatSessionHistoryDto>> getHistory(@RequestParam String phone) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);

        List<ChatSession> sessions =
                sessionRepo.findByPhoneAndStartedAtAfterOrderByStartedAtDesc(phone, since);

        List<ChatSessionHistoryDto> result = sessions.stream().map(s -> {
            List<ChatMessageDto> msgs =
                    chatRepo.findBySessionIdOrderByIdAsc(s.getId())
                            .stream()
                            .map(m -> new ChatMessageDto(
                                    m.getId(),
                                    m.getSender(),
                                    m.getMessage(),
                                    m.getCreatedAt()
                            ))
                            .collect(Collectors.toList());

            ChatSessionHistoryDto dto = new ChatSessionHistoryDto();
            dto.setSessionId(s.getId());
            dto.setStatus(s.getStatus());
            dto.setStartedAt(s.getStartedAt());
            dto.setLastActivityAt(s.getLastActivityAt());
            dto.setMessages(msgs);
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /* ========== 8) ADMIN – ACTIVE & CLOSED SESSIONS (30 days) ========== */

    @GetMapping("/sessions/active")
    public ResponseEntity<List<ChatSession>> getActiveSessions() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<ChatStatus> active = Arrays.asList(ChatStatus.NEW, ChatStatus.ACTIVE);

        List<ChatSession> list =
                sessionRepo.findByStatusInAndStartedAtAfterOrderByLastActivityAtDesc(active, since);

        return ResponseEntity.ok(list);
    }

    @GetMapping("/sessions/closed")
    public ResponseEntity<List<ChatSession>> getClosedSessions() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<ChatStatus> closed = Collections.singletonList(ChatStatus.CLOSED);

        List<ChatSession> list =
                sessionRepo.findByStatusInAndStartedAtAfterOrderByLastActivityAtDesc(closed, since);

        return ResponseEntity.ok(list);
    }
    @GetMapping("/sessions/closed/search")
    public ResponseEntity<List<ChatSession>> searchClosedSessions(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false, name = "fromDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "toDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        // Convert dates to LocalDateTime (start-of-day, and next-day for 'to')
        LocalDateTime from = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime to   = (toDate   != null) ? toDate.plusDays(1).atStartOfDay() : null;

        String phoneFilter = (phone == null || phone.isBlank()) ? null : phone.trim();

        // Baseline – how far back we want to search (e.g. last 90 days)
        LocalDateTime baseline = LocalDateTime.now().minusDays(90);

        List<ChatStatus> closed = Collections.singletonList(ChatStatus.CLOSED);

        // Get closed sessions from DB, then filter in Java
        List<ChatSession> allClosed =
                sessionRepo.findByStatusInAndStartedAtAfterOrderByLastActivityAtDesc(
                        closed, baseline
                );

        List<ChatSession> result = allClosed.stream()
                .filter(s -> {
                    if (phoneFilter == null) return true;
                    String p = s.getPhone();
                    return p != null && p.contains(phoneFilter);
                })
                .filter(s -> {
                    if (from == null) return true;
                    LocalDateTime started = s.getStartedAt();
                    return started != null && !started.isBefore(from);
                })
                .filter(s -> {
                    if (to == null) return true;
                    LocalDateTime started = s.getStartedAt();
                    return started != null && started.isBefore(to);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }


}
//package com.carwash.supportchatservice.controller;
//
//import com.carwash.supportchatservice.dto.*;
//import com.carwash.supportchatservice.entity.ChatMessage;
//import com.carwash.supportchatservice.entity.ChatSession;
//import com.carwash.supportchatservice.entity.ChatStatus;
//import com.carwash.supportchatservice.repository.ChatMessageRepository;
//import com.carwash.supportchatservice.repository.ChatSessionRepository;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/chat")
//@CrossOrigin(origins = "*")
//public class ChatController {
//
//    private final ChatMessageRepository chatRepo;
//    private final ChatSessionRepository sessionRepo;
//
//    public ChatController(ChatMessageRepository chatRepo,
//                          ChatSessionRepository sessionRepo) {
//        this.chatRepo = chatRepo;
//        this.sessionRepo = sessionRepo;
//    }
//
//    /* ========== 1) START CHAT SESSION (optional endpoint) ========== */
//
//    @PostMapping("/start")
//    public ResponseEntity<StartChatResponse> startChat(@RequestBody StartChatRequest req) {
//        if (req.getPhone() == null || req.getPhone().isBlank()) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        ChatSession session = new ChatSession();
//        session.setPhone(req.getPhone());
//        session.setUsername(req.getUsername());
//        session.setStatus(ChatStatus.NEW);
//        session = sessionRepo.save(session);
//
//        // optional first message
//        if (req.getFirstMessage() != null && !req.getFirstMessage().isBlank()) {
//            ChatMessage msg = new ChatMessage();
//            msg.setPhone(req.getPhone());
//            msg.setUsername(req.getUsername());
//            msg.setSender("USER");
//            msg.setMessage(req.getFirstMessage());
//            msg.setSessionId(session.getId());
//
//            ChatMessage saved = chatRepo.save(msg);
//
//            session.setLastActivityAt(saved.getCreatedAt());
//            sessionRepo.save(session);
//        }
//
//        StartChatResponse resp =
//                new StartChatResponse(session.getId(), session.getPhone(), session.getStatus());
//        return ResponseEntity.ok(resp);
//    }
//
//    /* Helper: find or create an open session for this phone */
//
//    private ChatSession getOrCreateOpenSession(String phone, String username) {
//        List<ChatStatus> openStatuses = Arrays.asList(ChatStatus.NEW, ChatStatus.ACTIVE);
//
//        Optional<ChatSession> existing =
//                sessionRepo.findTopByPhoneAndStatusInOrderByStartedAtDesc(phone, openStatuses);
//
//        if (existing.isPresent()) {
//            return existing.get();
//        }
//
//        ChatSession s = new ChatSession();
//        s.setPhone(phone);
//        s.setUsername(username);
//        s.setStatus(ChatStatus.NEW);
//        return sessionRepo.save(s);
//    }
//
//    /* ========== 2) USER SENDING MESSAGE ========== */
//
//    @PostMapping("/user-message")
//    public ResponseEntity<ChatMessage> userMessage(
//            @RequestBody ChatMessageRequest req,
//            @RequestParam(name = "sessionId", required = false) Long sessionId) {
//
//        if (req.getPhone() == null || req.getPhone().isBlank()) {
//            return ResponseEntity.badRequest().build();
//        }
//        if (req.getMessage() == null || req.getMessage().isBlank()) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        ChatSession session;
//        if (sessionId != null) {
//            session = sessionRepo.findById(sessionId)
//                    .orElseGet(() -> getOrCreateOpenSession(req.getPhone(), req.getUsername()));
//        } else {
//            session = getOrCreateOpenSession(req.getPhone(), req.getUsername());
//        }
//
//        ChatMessage msg = new ChatMessage();
//        msg.setPhone(req.getPhone());
//        msg.setUsername(req.getUsername());
//        msg.setSender("USER");
//        msg.setMessage(req.getMessage());
//        msg.setSessionId(session.getId());
//
//        ChatMessage saved = chatRepo.save(msg);
//
//     // If session is NEW (requested), keep it NEW until admin replies.
//     // Otherwise (for existing sessions), mark ACTIVE.
//     if (session.getStatus() != ChatStatus.NEW) {
//         session.setStatus(ChatStatus.ACTIVE);
//     }
//     session.setLastActivityAt(saved.getCreatedAt());
//     sessionRepo.save(session);
//
//
//        return ResponseEntity.ok(saved);
//    }
//
//    /* ========== 3) ADMIN SENDING MESSAGE ========== */
//
//    @PostMapping("/admin-message")
//    public ResponseEntity<ChatMessage> adminMessage(
//            @RequestBody ChatMessageRequest req,
//            @RequestParam(name = "sessionId", required = false) Long sessionId) {
//
//        if (req.getPhone() == null || req.getPhone().isBlank()) {
//            return ResponseEntity.badRequest().build();
//        }
//        if (req.getMessage() == null || req.getMessage().isBlank()) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        ChatSession session;
//        if (sessionId != null) {
//            session = sessionRepo.findById(sessionId)
//                    .orElseGet(() -> getOrCreateOpenSession(req.getPhone(), req.getUsername()));
//        } else {
//            session = getOrCreateOpenSession(req.getPhone(), req.getUsername());
//        }
//
//        ChatMessage msg = new ChatMessage();
//        msg.setPhone(req.getPhone());
//        msg.setUsername(req.getUsername());
//        msg.setSender("ADMIN");
//        msg.setMessage(req.getMessage());
//        msg.setSessionId(session.getId());
//
//        ChatMessage saved = chatRepo.save(msg);
//
//        session.setStatus(ChatStatus.ACTIVE);
//        session.setLastActivityAt(saved.getCreatedAt());
//        sessionRepo.save(session);
//
//        return ResponseEntity.ok(saved);
//    }
//
//    /* ========== 4) POLLING BY PHONE (used by existing HTML) ========== */
//
//    @GetMapping("/messages")
//    public ResponseEntity<List<ChatMessage>> getMessages(
//            @RequestParam String phone,
//            @RequestParam(name = "afterId", defaultValue = "0") Long afterId) {
//
//        // Load all messages for this phone, ordered by id
//        List<ChatMessage> all = chatRepo.findByPhoneOrderByIdAsc(phone);
//
//        // Filter in Java by afterId (simple & safe)
//        List<ChatMessage> filtered = all.stream()
//                .filter(m -> m.getId() != null && m.getId() > afterId)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(filtered);
//    }
//
//
//    /* ========== 5) END SESSION (user or admin) ========== */
//
//    @PostMapping("/end")
//    public ResponseEntity<Void> endChat(@RequestBody EndChatRequest req) {
//        ChatSession session = null;
//
//        if (req.getSessionId() != null) {
//            session = sessionRepo.findById(req.getSessionId()).orElse(null);
//        }
//
//        if (session == null && req.getPhone() != null) {
//            List<ChatStatus> openStatuses = Arrays.asList(ChatStatus.NEW, ChatStatus.ACTIVE);
//            session = sessionRepo
//                    .findTopByPhoneAndStatusInOrderByStartedAtDesc(req.getPhone(), openStatuses)
//                    .orElse(null);
//        }
//
//        if (session == null) {
//            return ResponseEntity.notFound().build();
//        }
//
//        session.setStatus(ChatStatus.CLOSED);
//        session.setLastActivityAt(LocalDateTime.now());
//        sessionRepo.save(session);
//
//        return ResponseEntity.ok().build();
//    }
//
//    /* ========== 6) USER CHAT HISTORY (last 30 days by phone) ========== */
//
//    @GetMapping("/history")
//    public ResponseEntity<List<ChatSessionHistoryDto>> getHistory(@RequestParam String phone) {
//        LocalDateTime since = LocalDateTime.now().minusDays(30);
//
//        List<ChatSession> sessions =
//                sessionRepo.findByPhoneAndStartedAtAfterOrderByStartedAtDesc(phone, since);
//
//        List<ChatSessionHistoryDto> result = sessions.stream().map(s -> {
//            List<ChatMessageDto> msgs =
//                    chatRepo.findBySessionIdOrderByIdAsc(s.getId())
//                            .stream()
//                            .map(m -> new ChatMessageDto(
//                                    m.getId(),
//                                    m.getSender(),
//                                    m.getMessage(),
//                                    m.getCreatedAt()
//                            ))
//                            .collect(Collectors.toList());
//
//            ChatSessionHistoryDto dto = new ChatSessionHistoryDto();
//            dto.setSessionId(s.getId());
//            dto.setStatus(s.getStatus());
//            dto.setStartedAt(s.getStartedAt());
//            dto.setLastActivityAt(s.getLastActivityAt());
//            dto.setMessages(msgs);
//            return dto;
//        }).collect(Collectors.toList());
//
//        return ResponseEntity.ok(result);
//    }
//
//    /* ========== 7) ADMIN – ACTIVE & CLOSED SESSIONS (last 30 days) ========== */
//
//    @GetMapping("/sessions/active")
//    public ResponseEntity<List<ChatSession>> getActiveSessions() {
//        LocalDateTime since = LocalDateTime.now().minusDays(30);
//        List<ChatStatus> active = Arrays.asList(ChatStatus.NEW, ChatStatus.ACTIVE);
//
//        List<ChatSession> list =
//                sessionRepo.findByStatusInAndStartedAtAfterOrderByLastActivityAtDesc(active, since);
//
//        return ResponseEntity.ok(list);
//    }
//
//    @GetMapping("/sessions/closed")
//    public ResponseEntity<List<ChatSession>> getClosedSessions() {
//        LocalDateTime since = LocalDateTime.now().minusDays(30);
//        List<ChatStatus> closed = Collections.singletonList(ChatStatus.CLOSED);
//
//        List<ChatSession> list =
//                sessionRepo.findByStatusInAndStartedAtAfterOrderByLastActivityAtDesc(closed, since);
//
//        return ResponseEntity.ok(list);
//    }
//}
