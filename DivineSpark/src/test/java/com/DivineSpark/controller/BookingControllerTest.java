package com.DivineSpark.controller;

import com.DivineSpark.model.SessionBooking;
import com.DivineSpark.service.BookingService;
import com.DivineSpark.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private BookingService bookingService;

    private SessionBooking sampleBooking;

    @BeforeEach
    void setUp() {
        sampleBooking = new SessionBooking();
        sampleBooking.setId(1L);
        sampleBooking.setBookingDate(LocalDateTime.now());
        sampleBooking.setPaymentStatus("FREE");
    }

    @Test
    void testBookFreeSession() throws Exception {
        Mockito.when(bookingService.bookFreeSession(anyLong(), anyLong()))
                .thenReturn(sampleBooking);

        mockMvc.perform(post("/api/book/free/{sessionId}", 1)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()) // mock JWT auth
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("FREE"));
    }

    @Test
    void testBookPaidSession() throws Exception {
        Mockito.when(bookingService.initiatePaidSessionBooking(anyLong(), anyLong()))
                .thenReturn("https://paymentgateway.com/pay?sessionId=101&userId=1");

        mockMvc.perform(post("/api/book/paid/{sessionId}", 1)
                        .with(SecurityMockMvcRequestPostProcessors.jwt())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("https://paymentgateway.com/pay?sessionId=101&userId=1"));
    }

    @Test
    void testConfirmPaidSessionBooking() throws Exception {
        Mockito.when(bookingService.confirmPaidSessionBooking(anyString(), anyLong(), anyLong()))
                .thenReturn(true);

        mockMvc.perform(post("/api/book/paid/confirm")
                        .param("paymentId", "abc123")
                        .param("sessionId", "1")
                        .with(SecurityMockMvcRequestPostProcessors.jwt())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking successful"));
    }
}
