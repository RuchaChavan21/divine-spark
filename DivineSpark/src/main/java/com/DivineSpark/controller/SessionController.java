package com.DivineSpark.controller;

import com.DivineSpark.model.Session;
import com.DivineSpark.model.SessionType;
import com.DivineSpark.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // ---------- ADMIN APIs

    @PostMapping("/admin")
    public ResponseEntity<Session> createSession(@RequestBody Session session) {
        log.info("Received request to create session: {}", session);
        try {
            Session created = sessionService.createSession(session);
            log.info("Session created successfully with ID: {}", created.getId());
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating session: {}", e.getMessage(), e);
            throw e; // rethrow so global handler can manage it
        }
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id, @RequestBody Session session) {
        log.info("Received request to update session with ID: {}", id);
        Session updated = sessionService.updateSession(id, session);
        log.info("Session updated successfully: {}", updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        log.info("Deleting session with ID: {}", id);
        sessionService.deleteSession(id);
        log.info("Session deleted successfully");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin")
    public ResponseEntity<List<Session>> getAllSessions() {
        log.info("Fetching all sessions");
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    // ---------- USER APIs

    @GetMapping("/active")
    public ResponseEntity<List<Session>> getActiveSessions() {
        log.info("Fetching all active sessions");
        return ResponseEntity.ok(sessionService.getActiveSessions());
    }

    @GetMapping("/active/type/{type}")
    public ResponseEntity<List<Session>> getActiveSessionsByType(@PathVariable SessionType type) {
        log.info("Fetching active sessions by type: {}", type);
        return ResponseEntity.ok(sessionService.getActiveSessionsByType(type));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Session>> getUpcomingSessions() {
        log.info("Fetching upcoming sessions");
        return ResponseEntity.ok(sessionService.getUpcomingSessions());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Session>> searchSessions(@RequestParam String keyword) {
        log.info("Searching sessions with keyword: {}", keyword);
        return ResponseEntity.ok(sessionService.searchSessionsByTitle(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> getSessionById(@PathVariable Long id) {
        log.info("Fetching session by ID: {}", id);
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }
}
