package com.DivineSpark.scheduler;

import com.DivineSpark.model.Session;
import com.DivineSpark.model.SessionBooking;
import com.DivineSpark.repository.BookingRepository;
import com.DivineSpark.repository.SessionRepository;
import com.DivineSpark.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionReminderScheduler {

    private final SessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;


    @Scheduled(cron = "0 0 8 * * ?")
    public void sendSessionReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusHours(24);


        List<Session> upcomingSessions = sessionRepository.findByStartTimeAfter(now);
        for (Session session : upcomingSessions) {
            if (session.getStartTime().isBefore(next24Hours) && session.isActive()) {
                List<SessionBooking> bookings = bookingRepository.findBySession(session);
                for (SessionBooking booking : bookings) {
                    String username = booking.getUser().getUsername();
                    String email = booking.getUser().getEmail();

                    String subject = "Reminder: Your session '" + session.getTitle() + "' is tomorrow!";
                    String body = "Hello " + username + ",\n\n" +
                            "This is a reminder that your session '" + session.getTitle() + "' " +
                            "is scheduled to start at " + session.getStartTime() + ".\n\n" +
                            "Join link: " + booking.getJoinToken() + "\n\n" +
                            "Regards,\nDivine Spark Team";

                    emailService.sendBookingEmail(email, subject, body);
                }
            }
        }
        log.info("Session reminders sent for sessions happening in next 24 hours.");
    }
}
