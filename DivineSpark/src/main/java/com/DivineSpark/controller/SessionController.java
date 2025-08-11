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

    // ---------------- ADMIN APIs (Require ROLE_ADMIN) ---------------- //

    @PostMapping("/admin")
    public ResponseEntity<Session> createSession(@RequestBody Session session) {
        log.info("Admin creating session: {}", session);
        Session created = sessionService.createSession(session);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id, @RequestBody Session session) {
        log.info("Admin updating session ID: {}", id);
        Session updated = sessionService.updateSession(id, session);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        log.info("Admin deleting session ID: {}", id);
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin")
    public ResponseEntity<List<Session>> getAllSessionsForAdmin() {
        log.info("Admin fetching ALL sessions (active/inactive)");
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    // ---------------- PUBLIC APIs (No login required) ---------------- //

    @GetMapping
    public ResponseEntity<List<Session>> getActiveSessions() {
        log.info("Fetching active sessions (public)");
        return ResponseEntity.ok(sessionService.getActiveSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> getSessionById(@PathVariable Long id) {
        log.info("Fetching session by ID (public): {}", id);
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Session>> getUpcomingSessions() {
        log.info("Fetching upcoming sessions (public)");
        return ResponseEntity.ok(sessionService.getUpcomingSessions());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Session>> searchSessions(@RequestParam String keyword) {
        log.info("Public search for sessions with keyword: {}", keyword);
        return ResponseEntity.ok(sessionService.searchSessionsByTitle(keyword));
    }

    @GetMapping("/active/type/{type}")
    public ResponseEntity<List<Session>> getActiveSessionsByType(@PathVariable SessionType type) {
        log.info("Fetching active sessions by type (public): {}", type);
        return ResponseEntity.ok(sessionService.getActiveSessionsByType(type));
    }
}
