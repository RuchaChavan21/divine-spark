package com.DivineSpark.repository;

import com.DivineSpark.model.Session;
import com.DivineSpark.model.SessionBooking;
import com.DivineSpark.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<SessionBooking,Long> {

    long countBySession(Session session);

    boolean existsBySessionAndUserId(Session session, Long userId);

    Optional<SessionBooking> findByJoinToken(String joinToken);

    Optional<SessionBooking> findByUserAndSession(User user, Session session);

}
