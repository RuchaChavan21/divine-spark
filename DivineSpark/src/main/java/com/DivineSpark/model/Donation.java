package com.DivineSpark.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "donationsMoney") // Good practice to name the table explicitly
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false) // Emails should typically be unique
    private String email;

    private String phone;

    /**
     * Use @ElementCollection to map a collection of basic types like String.
     * This tells JPA to create a separate table (e.g., donation_towards)
     * to hold the set of causes for each donation.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "donation_towards", joinColumns = @JoinColumn(name = "donation_id"))
    @Column(name = "cause")
    private Set<String> towards;

    /**
     * It is best practice to use BigDecimal for monetary values
     * to avoid floating-point precision issues that can occur with double or float.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // Auditing fields are useful for tracking record history
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Builder
    public Donation(String firstName, String lastName, String email, String phone,
                    BigDecimal amount, PaymentStatus paymentStatus) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }
}