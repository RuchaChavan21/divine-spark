package com.DivineSpark.ServiceImpl;

import com.DivineSpark.model.Session;
import com.DivineSpark.model.SessionType;
import com.DivineSpark.repository.SessionRepository;
import com.DivineSpark.service.SessionService;
import jakarta.persistence.EntityNotFoundException;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;


    @Override
    public Session createSession(Session session) {
        session.setActive(true);
        return sessionRepository.save(session);
    }

    @Override
    public Session updateSession(Long sessionId, Session session) {
        Session existingSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with ID: " + sessionId));

        existingSession.setTitle(session.getTitle());
        existingSession.setDescription(session.getDescription());
        existingSession.setStartTime(session.getStartTime());
        existingSession.setEndTime(session.getEndTime());
        existingSession.setType(session.getType());
        existingSession.setPrice(session.getPrice());
        existingSession.setZoomLink(session.getZoomLink());
        existingSession.setActive(session.isActive());

        return sessionRepository.save(existingSession);
    }

    @Override
    public void deleteSession(Long sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new EntityNotFoundException("Session not found with ID: " + sessionId);
        }
        sessionRepository.deleteById(sessionId);
    }

    @Override
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public List<Session> getActiveSessions() {
        return sessionRepository.findByActiveTrue();
    }

    @Override
    public List<Session> getActiveSessionsByType(SessionType type) {
        return sessionRepository.findByActiveTrueAndType(type);
    }

    @Override
    public List<Session> getUpcomingSessions() {
        return sessionRepository.findByStartTimeAfter(LocalDateTime.now());
    }

    @Override
    public List<Session> searchSessionsByTitle(String keyword) {
        return sessionRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    public Session getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with ID: " + id));
    }
}