package com.DivineSpark.controller;

import com.DivineSpark.dto.DonationDTO;
import com.DivineSpark.dto.RazorpayOrderResponse;
import com.DivineSpark.model.Donation;
import com.DivineSpark.model.PaymentStatus;
import com.DivineSpark.repository.DonationRepository;
import com.DivineSpark.service.DonationService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationRepository donationRepository;
    private final DonationService donationService; // Added service for CRUD operations

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    // --- PAYMENT FLOW ENDPOINTS ---

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody DonationDTO donationRequest) {
        try {
            // 1. Create and save a PENDING donation record in your database
            Donation donation = new Donation();
            donation.setFirstName(donationRequest.getFirstName());
            donation.setLastName(donationRequest.getLastName());
            donation.setEmail(donationRequest.getEmail());
            donation.setPhone(donationRequest.getPhone());
            donation.setAmount(donationRequest.getAmount());
            donation.setPaymentStatus(PaymentStatus.PENDING);
            Donation pendingDonation = donationRepository.save(donation);

            // 2. Create a Razorpay order
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            long amountInPaise = donationRequest.getAmount().multiply(new BigDecimal(100)).longValue();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "donation_rcpt_" + pendingDonation.getId());

            Order order = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            // 3. Update your donation record with the Razorpay order ID
            pendingDonation.setRazorpayOrderId(razorpayOrderId);
            donationRepository.save(pendingDonation);

            // 4. Return the necessary details to the frontend
            RazorpayOrderResponse response = new RazorpayOrderResponse(
                    razorpayOrderId,
                    "INR",
                    amountInPaise,
                    razorpayKeyId
            );

            return ResponseEntity.ok(response);

        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error creating Razorpay order."));
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest verificationRequest) {
        try {
            String secret = this.razorpayKeySecret;
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", verificationRequest.getRazorpay_order_id());
            options.put("razorpay_payment_id", verificationRequest.getRazorpay_payment_id());
            options.put("razorpay_signature", verificationRequest.getRazorpay_signature());

            boolean isSignatureValid = Utils.verifyPaymentSignature(options, secret);

            if (isSignatureValid) {
                Donation donation = donationRepository.findByRazorpayOrderId(verificationRequest.getRazorpay_order_id())
                        .orElseThrow(() -> new RuntimeException("Donation not found for this order ID"));

                donation.setPaymentStatus(PaymentStatus.COMPLETED);
                donationRepository.save(donation);

                return ResponseEntity.ok(Map.of("status", "success", "message", "Payment verified successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "failure", "message", "Payment verification failed."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // --- STANDARD CRUD ENDPOINTS (for admin use) ---

    @GetMapping
    public ResponseEntity<List<DonationDTO>> getAllDonations() {
        List<DonationDTO> donations = donationService.getAllDonations();
        return ResponseEntity.ok(donations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationDTO> getDonationById(@PathVariable Long id) {
        DonationDTO donation = donationService.getDonationById(id);
        return ResponseEntity.ok(donation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DonationDTO> updateDonation(@PathVariable Long id, @RequestBody DonationDTO donationDTO) {
        DonationDTO updatedDonation = donationService.updateDonation(id, donationDTO);
        return ResponseEntity.ok(updatedDonation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonation(@PathVariable Long id) {
        donationService.deleteDonation(id);
        return ResponseEntity.noContent().build();
    }

    // --- DTO for verification request ---
    @Data
    static class PaymentVerificationRequest {
        private String razorpay_order_id;
        private String razorpay_payment_id;
        private String razorpay_signature;
    }
}
