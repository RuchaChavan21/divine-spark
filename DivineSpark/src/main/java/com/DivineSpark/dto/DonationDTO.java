package com.DivineSpark.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class DonationDTO {

    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Set<String> towards;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}