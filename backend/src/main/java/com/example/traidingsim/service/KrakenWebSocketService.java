package com.example.traidingsim.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ClientEndpoint
@Service
@Slf4j
public class KrakenWebSocketService {

    private static final String KRAKEN_WEBSOCKET_URI = "wss://ws.kraken.com/v2";
    @Getter
    private final Map<String, Double> cryptoPrices = new ConcurrentHashMap<>();

    private Session session;

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public KrakenWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
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
        this.session = session;
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

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(message);

            if (jsonNode.has("data") && jsonNode.get("data").isArray()) {
                JsonNode dataNode = jsonNode.get("data").get(0);
                String symbol = dataNode.get("symbol").asText();
                double lastPrice = dataNode.get("last").asDouble();

                cryptoPrices.put(symbol, lastPrice);

                log.info("Updated price for {}: ${}", symbol, lastPrice);

                messagingTemplate.convertAndSend("/topic/prices", Map.of("symbol", symbol, "price", lastPrice));

            }
        } catch (Exception e) {
            log.error("Error processing Websocket message", e);
        }
    }

}