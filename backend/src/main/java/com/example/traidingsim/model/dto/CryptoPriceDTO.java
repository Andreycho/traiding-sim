package com.example.traidingsim.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CryptoPriceDTO {
    private String symbol;
    private Double price;
}