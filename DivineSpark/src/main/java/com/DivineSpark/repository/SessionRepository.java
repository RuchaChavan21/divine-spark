package com.DivineSpark.repository;

import com.DivineSpark.model.Session;
import com.DivineSpark.model.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>, JpaSpecificationExecutor<Session> {

    // Find all active sessions
    List<Session> findByActiveTrue();

    // Find active sessions by type (FREE or PAID)
    List<Session> findByActiveTrueAndType(SessionType type);

    // Find sessions starting after a given date/time
    List<Session> findByStartTimeAfter(LocalDateTime dateTime);

    // Find sessions by title containing keyword (case-insensitive)
    List<Session> findByTitleContainingIgnoreCase(String keyword);
}
