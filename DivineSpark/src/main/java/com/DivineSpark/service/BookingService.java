package com.DivineSpark.service;

import com.DivineSpark.model.SessionBooking;

public interface BookingService {

    SessionBooking bookFreeSession(Long sessionId, Long userId);

    String initiatePaidSessionBooking(Long sessionId, Long userId);

    boolean confirmPaidSessionBooking(String paymentId,Long sessionId, Long userId);
}
