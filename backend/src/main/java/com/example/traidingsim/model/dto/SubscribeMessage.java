package com.example.traidingsim.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeMessage {

    private String method = "subscribe";

    @JsonProperty("params")
    private Map<String, Object> params;

    public static SubscribeMessage createDefaultSubscription() {
        return new SubscribeMessage(
                "subscribe",
                Map.of(
                        "channel", "ticker",
                        "symbol", List.of("BTC/USD", "MATIC/GBP", "ETH/USD", "ADA/USD", "SOL/USD", "ALGO/USD")
                )
        );
    }
}
