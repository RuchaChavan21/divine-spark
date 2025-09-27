package com.DivineSpark.dto;

public record BookingResponse(Long bookingId, Long sessionId, String title, String confirmationMessage) {
}
