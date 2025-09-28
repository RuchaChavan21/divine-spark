package com.DivineSpark.ServiceImpl;

import com.DivineSpark.model.Session;
import com.DivineSpark.model.SessionBooking;
import com.DivineSpark.model.SessionType;
import com.DivineSpark.repository.BookingRepository;
import com.DivineSpark.repository.SessionRepository;
import com.DivineSpark.service.EmailService;
import com.DivineSpark.service.SessionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final Path rootLocation;

    public SessionServiceImpl(SessionRepository sessionRepository,
                              BookingRepository bookingRepository,
                              EmailService emailService) {
        // Inject the Spring beans
        this.sessionRepository = sessionRepository;
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;

        // Manually create the Path object
        this.rootLocation = Paths.get("uploads");

        // Create the directory if it doesn't exist
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public Session createSession(Session session, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = imageFile.getOriginalFilename();
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();

            try (InputStream inputStream = imageFile.getInputStream()) {
                // Save the file
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file.", e);
            }

            // Set the URL path to be stored in the database
            // This will be something like "/uploads/my-image.png"
            session.setImageUrl("/uploads/" + filename);
        }

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

    @Override
    @Transactional
    public void cancelSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with ID: " + sessionId));

        // 1️⃣ Deactivate the session
        session.setActive(false);
        sessionRepository.save(session);

        // 2️⃣ Fetch all bookings for this session
        List<SessionBooking> bookings = bookingRepository.findBySession(session);

        // 3️⃣ Send cancellation emails to all booked users
        for (SessionBooking booking : bookings) {
            String username = booking.getUser().getUsername();
            String email = booking.getUser().getEmail();
            emailService.sendBookingEmail(
                    email,
                    "Session Cancelled: " + session.getTitle(),
                    "Hello " + username + ",\n\n" +
                            "We regret to inform you that the session '" + session.getTitle() +
                            "' scheduled on " + session.getStartTime() +
                            " has been cancelled.\n\n" +
                            "If you have paid for this session, your refund will be processed within 7 days.\n\n" +
                            "Regards,\nDivine Spark Team"
            );
        }

        // 4️⃣ Mark all bookings as CANCELLED
        for (SessionBooking booking : bookings) {
            booking.setPaymentStatus("CANCELLED");
        }
        bookingRepository.saveAll(bookings);
    }

    @Override
    public Page<Session> getSessionWithPaginationAndfiltering(int page, int size, String sortBy, String sortDir, String keyword) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Session> spec = (root, query, cb) -> {
            if (keyword != null && !keyword.isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                return cb.or(cb.like(cb.lower(root.get("title")), likePattern),
                        cb.like(cb.lower(root.get("description")), likePattern)
                );
            }
            return cb.conjunction();
        };
        return sessionRepository.findAll(spec, pageable);
    }
}
