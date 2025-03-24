package com.example.traidingsim.websocket;

import com.example.traidingsim.model.dto.CryptoPriceDTO;
import com.example.traidingsim.mapper.CryptoPriceMapper;
import com.example.traidingsim.model.dto.CryptoPricePayloadDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@ClientEndpoint
@Service
@Slf4j
public class KrakenWebSocketService {

    private static final String KRAKEN_WEBSOCKET_URI = "wss://ws.kraken.com/v2";
    private final ObjectMapper objectMapper;
    private final FrontendWebSocketService frontendWebSocketService;
    private final Map<String, Double> cryptoPrices = new ConcurrentHashMap<>();

    public KrakenWebSocketService(ObjectMapper objectMapper, FrontendWebSocketService frontendWebSocketService) {
        this.objectMapper = objectMapper;
        this.frontendWebSocketService = frontendWebSocketService;
        connectToKrakenWebSocket();
    }

    private void connectToKrakenWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(KRAKEN_WEBSOCKET_URI));
            log.info("Connected to Kraken WebSocket");
        } catch (Exception e) {
            log.error("Error connecting to Kraken WebSocket: {}", e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        String subscribeMessage = """
                {
                    "method": "subscribe",
                    "params": {
                        "channel": "ticker",
                        "symbol": [
                            "BTC/USD", "MATIC/GBP", "ETH/USD", "ADA/USD", "SOL/USD", "ALGO/USD"
                        ]
                    }
                }
        """;
        session.getAsyncRemote().sendText(subscribeMessage);
        log.info("Subscribed to Kraken ticker channel");
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            log.debug("Received message: {}", message);

            JsonNode jsonNode = objectMapper.readTree(message);

            if (jsonNode.has("data") && jsonNode.get("data").isArray()) {
                JsonNode dataNode = jsonNode.get("data").get(0);

                CryptoPricePayloadDTO payload = new CryptoPricePayloadDTO();
                payload.setSymbol(dataNode.get("symbol").asText());
                payload.setLastPrice(dataNode.get("last").asDouble());

                CryptoPriceDTO dto = CryptoPriceMapper.toDTO(payload);

                frontendWebSocketService.broadcastCryptoPrice(dto);

                cryptoPrices.put(payload.getSymbol(), payload.getLastPrice());
                log.info("Updated price for {}: {}", payload.getSymbol(), payload.getLastPrice());
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
        }
    }

    public Map<String, Double> getCryptoPrices() {
        return Collections.unmodifiableMap(cryptoPrices);
    }
}