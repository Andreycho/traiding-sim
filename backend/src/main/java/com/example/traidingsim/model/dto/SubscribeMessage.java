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
                        "symbol", List.of(
                                "BTC/USD", "ETH/USD", "BNB/USD", "XRP/USD", "ADA/USD",
                                "DOGE/USD", "SOL/USD", "DOT/USD", "MATIC/USD", "LTC/USD",
                                "SHIB/USD", "AVAX/USD", "UNI/USD", "XLM/USD", "BCH/USD",
                                "ALGO/USD", "VET/USD", "ICP/USD", "MANA/USD", "AXS/USD"
                        )
                )
        );
    }
}
