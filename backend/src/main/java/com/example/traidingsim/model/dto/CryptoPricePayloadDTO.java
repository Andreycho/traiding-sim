package com.example.traidingsim.model.dto;

import lombok.Data;

@Data
public class CryptoPricePayloadDTO {
    private String symbol;
    private Double lastPrice;
}