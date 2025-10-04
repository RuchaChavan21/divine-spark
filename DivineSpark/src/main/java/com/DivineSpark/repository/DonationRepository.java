package com.DivineSpark.repository;

import com.DivineSpark.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    Optional<Donation> findByEmail(String email);

    // Find a donation record using the Razorpay Order ID
    Optional<Donation> findByRazorpayOrderId(String orderId);

}
