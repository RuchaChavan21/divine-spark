package com.DivineSpark.dto;

public record RazorpayOrderResponse(String orderId, String currency, long amount, String key) {
}
