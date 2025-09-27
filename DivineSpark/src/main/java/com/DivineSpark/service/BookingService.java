package com.DivineSpark.service;

import com.DivineSpark.model.SessionBooking;

import java.util.Map;

public interface BookingService {

    SessionBooking bookFreeSession(Long sessionId, Long userId);

    Map<String, Object> initiatePaidSessionBooking(Long sessionId, Long userId);

    boolean confirmPaidSessionBooking(String paymentId,Long sessionId, Long userId);
}
