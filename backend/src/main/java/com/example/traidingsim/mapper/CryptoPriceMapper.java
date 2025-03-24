package com.example.traidingsim.mapper;

import com.example.traidingsim.model.dto.CryptoPriceDTO;
import com.example.traidingsim.model.dto.CryptoPricePayloadDTO;

public class CryptoPriceMapper {

    public static CryptoPriceDTO toDTO(CryptoPricePayloadDTO payload) {
        return CryptoPriceDTO.builder()
                .symbol(payload.getSymbol())
                .price(payload.getLastPrice())
                .build();
    }
}