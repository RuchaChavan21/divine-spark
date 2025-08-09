package com.DivineSpark.service;

import com.DivineSpark.model.Session;
import com.DivineSpark.model.SessionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface SessionService {


    Session createSession(Session session);


    Session updateSession(Long sessionId, Session session);


    void deleteSession(Long sessionId);


    List<Session> getAllSessions();


    List<Session> getActiveSessions();


    List<Session> getActiveSessionsByType(SessionType type);


    List<Session> getUpcomingSessions();


    List<Session> searchSessionsByTitle(String keyword);


    Session getSessionById(Long id);
}
