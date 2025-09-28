package com.DivineSpark.service;

import com.DivineSpark.model.Session;
import com.DivineSpark.model.SessionType;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface SessionService {


    Session createSession(Session session, MultipartFile imageFile);


    Session updateSession(Long sessionId, Session session);


    void deleteSession(Long sessionId);


    List<Session> getAllSessions();


    List<Session> getActiveSessions();


    List<Session> getActiveSessionsByType(SessionType type);


    List<Session> getUpcomingSessions();


    List<Session> searchSessionsByTitle(String keyword);


    Session getSessionById(Long id);

    void cancelSession(Long sessionId);

    Page<Session> getSessionWithPaginationAndfiltering(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String keyword
    );
}
